Ext.onReady(function() {
    var selectedFilePath = "";
    var partViewed;
    var lineForPage = 500;
    var lastPage = false;

    var treeFromRoot;

    var loc = location.href;
    loc = loc.substring(0, loc.lastIndexOf('/'));

    var searchRunning = false;
    var searchResult;
    var pos;
    var searchRootDir;
    var searchRequestLength;
    var searchViewCurPage;
    var searchLastPage = false;
    var searchResApps = [];
    var searchResPages = [];
    var searchResCurApp;
    var searchResCurPage;
    var searchResCurOcc;
    var searchBytesToLightFromPrevPage = 0;
    var searchPageToLightFirstBytes = -1;

    var solrSearchOccurrences = [];
    var isSolrSearch = false;
    var isGrepOverSolr = false;
    
    var next = Ext.create('Ext.Button', {
        text: 'Next',
        width: 100,
        renderTo: 'nextEl',
        handler: function() {
            writeText('next');
        }
    });
    
    var previous = Ext.create('Ext.Button', {
        text: 'Previous',
        width: 100,
        renderTo: 'prevEl',
        handler: function() {
            writeText('prev');
        }
    });
    
    var previous = Ext.create('Ext.Button', {
        text: 'Alerts',
        width: 100,
        renderTo: 'alertsEl',
        handler: function() {
            window.location = loc + "/alerts.html";
        }
    });

    Ext.Ajax.request({
        url : loc + '/logtool',
        params : {
            action : 'gettree'
        },
        method : 'GET',
        success : function(result, request) {
            var treeFromRoot = Ext.JSON.decode(result.responseText);

            var treestore = Ext.create('Ext.data.TreeStore', {
                        root : {
                            text : 'File System',
                            expanded : true,
                            children : treeFromRoot
                        }
                    });

            var contextMenu = new Ext.menu.Menu({
                        items : [{
                                    text : 'Delete',
                                    handler : deleteHandler
                                }]
                    });

            var searchField = new Ext.form.TextField({
                                    id: 'searchValue',
                                    fieldLabel: 'Search request',
                                    width: 489
                                });

            var searchResData = [];

            var fields = [
                {name: 'appspec', mapping : 'appspec'},
                {name: 'occurrences', mapping : 'occurrences'},
            ];

            var searchGridStore = new Ext.data.JsonStore({
                fields: fields,
                data: searchResData
            });


            var columns = [
                {id : 'appSpec', header: "Application specification", width: 325, sortable: false, dataIndex: 'appspec'},
                {header: "Occurrences/Message", flex: 1, sortable: false, dataIndex: 'occurrences'}
            ];

            var searchResGrid = new Ext.grid.GridPanel({
                renderTo: 'div3',
                id: 'searchResGrid',
                store: searchGridStore,
                columns: columns,
                anchor: '100%',
                width: '100%',
                height: '100%',
                title: 'Search results',
                viewConfig: {
                    stripeRows: true
                },
                listeners: {
                    itemdblclick: function(dataView, record, item, index, e) {
                        if (!isSolrSearch) {
                            searchResCurApp = index;
                            searchResCurPage = 0;
                            searchResCurOcc = 0;
                            searchBytesToLightFromPrevPage = 0;
                            searchPageToLightFirstBytes = -1;
                            searchLastPage = false;

                            searchViewCurPage = parseInt(searchResPages[searchResCurPage]);

                            printNewPage();
                        } else {
                            searchResCurApp = index;
                            updateSolrSearchPagePos();
                            printNewPage();
                        }
                    }
                }
            });

            var treePanel = new Ext.tree.TreePanel({
                        id : 'treePanel',
                        renderTo : 'div1',
                        rootVisible : false,
                        layout: 'fit',
                        anchor: '100%',
                        height: '100%',
                        store : treestore,
                        listeners : {
                            itemclick : function(view, record, item,
                                    index, e) {
                                if (record.isLeaf()) {
                                    searchRunning = false;
                                    selectedFilePath = getFilePath(record);
                                    partViewed = -1;
                                    writeText(selectedFilePath);
                                }
                            },
                            itemcontextmenu : function(view, record,
                                    item, index, e) {
                                e.stopEvent();
                                contextMenu.showAt(e.getXY());
                            }
                        },
                        dockedItems : [{
                                    xtype : 'toolbar',
                                    items : [
                                    'Actions with checked items:',
                                    '->', 
                                        {
                                            icon: 'extjs/resources/delete.gif',
                                            text: 'Delete',
                                            handler : deleteHandler
                                        },
                                        {
                                            icon: 'thirdparty/magnifier.png',
                                            text: 'Search',
                                            handler: openSearchWindow
                                        }
                                    ]
                                }]
                    });

            var panelMap = new Ext.KeyMap(Ext.get('treePanel'), {
                    key: [Ext.EventObject.DELETE, Ext.EventObject.BACKSPACE],
                    fn: deleteHandler
                });

            var textFieldMap = new Ext.KeyMap(Ext.get('searchValue'), {
                    key: Ext.EventObject.ENTER,
                    fn: doSearch
                });

            function deleteHandler() {
                var view = treePanel.getView();
                var chkNodes = view.getChecked();
                if (chkNodes.length > 0) {
                    text = chkNodes.length > 1 ? chkNodes.length
                            + ' items' : '\'' + chkNodes[0].get('text')
                            + '\'';
                    Ext.MessageBox.confirm('Delete confirmation',
                            'Do you really want to delete ' + text
                                    + '?', function(btn) {
                                if (btn == 'yes') {
                                    while (chkNodes.length > 0) {
                                        deleteNode(chkNodes[0]);
                                        chkNodes = view.getChecked();
                                    }
                                }
                            });
                }
            };

            function getFilePath(record) {
                selectedFilePath = record.get('text');
                node = record.parentNode;
                while (!node.isRoot()) {
                    selectedFilePath = selectedFilePath + '/'
                            + node.get('text');
                    node = node.parentNode;
                }
                return selectedFilePath;
            };

            function deleteNode(selNode) {
                selectedPath = getFilePath(selNode);

                if (selNode.isLeaf()) {
                    document.getElementById('div2').innerHTML = '';
                    Ext.Ajax.request({
                                url : loc + '/logtool',
                                params : {
                                    action : 'deletelog',
                                    path : selectedPath
                                },
                                method : 'GET'
                            });
                } else {
                    Ext.Ajax.request({
                                url : loc + '/logtool',
                                params : {
                                    action : 'deletedirectory',
                                    path : selectedPath
                                },
                                method : 'GET'
                            });
                }

                node = selNode.parentNode;
                selNode.remove();

                while (!node.hasChildNodes() && !node.isRoot()) {
                    temp = node;
                    node = node.parentNode;
                    node.removeChild(temp);
                }
            };

            function openSearchWindow() {
                var searchWindow = new Ext.Window({
                    title: 'Search',
                    resizable: false,
                    width: 500,
                    items: searchField,
                    listeners: {
                        destroy: onCancel
                    },
                    buttons: [{
                        text: 'Prev page',
                        handler: prevPage
                    },{
                        text: 'Next page',
                        handler: nextPage
                    },{
                        text: 'Prev',
                        handler: prev
                    },{
                        text: 'Next',
                        handler: next
                    },{
                        text: 'Search',
                        handler: doSearch
                    },{
                        text: 'Cancel',
                        handler: function() {
                            searchWindow.close();
                        }
                    }]
                });
                searchWindow.show();
            };

            function onCancel() {
                clearDiv();
                searchRunning = false;
                var searchResApps = [];
                var searchResPages = [];
                searchGridStore.removeAll();
                searchField = new Ext.form.TextField({
                        id: 'searchValue',
                        fieldLabel: 'Search request',
                        width: 489
                    });

                isSolrSearch = false;
                isGrepOverSolr = false;;
                solrSearchOccurrences = [];
            };

            function doSearch() {
                clearDiv();
                searchGridStore.removeAll();

                var grepIndex = searchField.getValue().indexOf('grep: ');

                if (grepIndex == -1) {
                    doSolrSearch(searchField.getValue());
                } else if (grepIndex > 0) {
                    doGrepSolrSearch(searchField.getValue().substring(0, grepIndex - 1), searchField.getValue().substring(grepIndex + 6));
                } else {
                    doGrepSearch();
                }
            };

            function doGrepSearch() {
                isSolrSearch = false;
                isGrepOverSolr = false;
                searchRunning = true;

                var selModel = treePanel.getSelectionModel();
                var selNodes = selModel.getSelection();
                if (selNodes.length > 0) {
                    var selNode = selNodes[0];
                    var path = getFilePath(selNode);
                    searchRootDir = path;
                    var searchRequest = searchField.getValue().substring(6);
                    searchRequestLength = searchRequest.length;

                    Ext.Ajax.request({
                        url : loc + '/logtool' ,
                        params : {
                            action: 'doSearch',
                            path: path,
                            searchRequest: searchRequest,
                            pageSize: lineForPage
                            },
                        method: 'GET',
                        success: function (result, request) {
                            initSearchRes(result);
                        },
                        failure: function (result, request) {
                            Ext.MessageBox.alert('Failed', result.responseText);
                        }
                    });
                }
            };

            function doSolrSearch(query) {
                isSolrSearch = true;
                searchRunning = true;
                Ext.Ajax.request({
                    url : loc + '/logtool' ,
                    params : {
                        action: 'doSolrSearch',
                        subaction: 'solrsearch',
                        query: query
                    },
                    method: 'GET',
                    success: function (result, request) {
                        eval(replaceStringDelimiters(result.responseText));
                        solrSearchOccurrences = occurrences;
                        if (solrSearchOccurrences.length == 0) {
                            Ext.MessageBox.show({
                                title: 'Search results',
                                msg: 'Nothing found.', buttons: Ext.MessageBox.OK
                            });
                            return;
                        }
                        addSearchResultToGrid();

                        searchResCurApp = 0;
                        updateSolrSearchPagePos();
                        printNewPage();
                    },
                    failure: function (result, request) {
                        Ext.MessageBox.alert('Failed', result.responseText);
                    }
                });
            };

            function doGrepSolrSearch(query, request) {
                isSolrSearch = false
                isGrepOverSolr = true;
                searchRunning = true;
                searchRequestLength = request.length;

                Ext.Ajax.request({
                    url : loc + '/logtool' ,
                    params : {
                        action: 'doSolrSearch',
                        subaction: 'grepOverSolr',
                        query: query,
                        request: request,
                        pageSize: lineForPage
                    },
                    method: 'GET',
                    success: function (result, request) {
                        initSearchRes(result);
                    },
                    failure: function (result, request) {
                        Ext.MessageBox.alert('Failed', result.responseText);
                    }
                });
            };

            function initSearchRes(result) {
                searchResult = getSearchResult(result.responseText);

                searchResApps = [];
                for (app in searchResult) {
                    searchResApps.push(app);
                };
                searchResCurApp = 0;

                updateSearchPagePos('next');

                addSearchResultToGrid();
                printNewPage();
            };

            function addSearchResultToGrid() {
                if (!isSolrSearch) {
                    if (searchResApps.length != 0) {
                        if (!isGrepOverSolr) {
                            for (resApp in searchResult) {
                                var reversedSearchDir = reversePath(searchRootDir);
                                reversedSearchDir = reversedSearchDir.substring(0, reversedSearchDir.length - 1);
                                var app = resApp.substring(resApp.indexOf(reversedSearchDir), resApp.length);
                                searchGridStore.add({appspec: app, occurrences: countOccurrences(resApp)});
                            }
                        } else {
                            for (resApp in searchResult) {
                                searchGridStore.add({appspec: resApp.substring(resApp.indexOf('||<>||') + 6), occurrences: countOccurrences(resApp)});
                            }
                        }
                    } else {
                        Ext.MessageBox.show({
                            title: 'Search results',
                            msg: 'nothing found.', buttons: Ext.MessageBox.OK
                        });
                    }
                } else {
                    addOneRow(0);
                }
            };

            function addOneRow(index) {
                if (index == solrSearchOccurrences.length) {
                    return;
                }

                var length = 25;
                if (parseInt(solrSearchOccurrences[index].length) < 25) {
                    length = solrSearchOccurrences[index].length;
                }
                Ext.Ajax.request({
                    url : loc + '/logtool' ,
                    params : {
                        action : 'getLog',
                        path: reversePath(solrSearchOccurrences[index].path),
                        partToView: solrSearchOccurrences[index].startIndex,
                        lines: length
                    },
                    method: 'GET',
                    success: function (result, request) {
                        var resp = replaceStringDelimiters(result.responseText);
                        eval(resp);
                        var appspec = solrSearchOccurrences[index].application + ' / ' +
                                solrSearchOccurrences[index].host + ' / ' +
                                solrSearchOccurrences[index].instance + ' / ' +
                                solrSearchOccurrences[index].date +
                                '    (' + solrSearchOccurrences[index].startIndex + ', ' + solrSearchOccurrences[index].length + ')';
                        var msg = response.log + ' ...';
                        searchGridStore.add({
                                appspec: appspec,
                                occurrences: msg
                            });
                        addOneRow(index + 1);
                    },
                    failure: function (result, request) {
                        Ext.MessageBox.alert('Failed', result.responseText);
                    }
                });
            }

            function countOccurrences(app) {
                var pages = searchResult[app];
                var res = 0;
                for (page in pages) {
                    res += pages[page].length;
                }
                return res;
            };

            function next() {
                if (!isSolrSearch) {
                    if (searchResApps.length > 0 && searchRunning) {
                        var occurrences = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]];
                        if (searchResCurOcc + 1 == occurrences.length) {
                            if (searchResCurPage + 1 == searchResPages.length) {
                                if (searchResCurApp + 1 != searchResApps.length) {
                                    searchResCurApp++;
                                    updateSearchPagePos('next');
                                    printNewPage();
                                }
                            } else {
                                searchResCurPage++;
                                searchResCurOcc = 0;
                                searchViewCurPage = parseInt(searchResPages[searchResCurPage]);
                                printNewPage();
                            }
                        } else {
                            searchResCurOcc++;
                            printCurPage();
                        }
                    }
                } else {
                    if (searchResCurApp + 1 != solrSearchOccurrences.length && searchRunning) {
                        searchResCurApp++;
                        updateSolrSearchPagePos();
                        printNewPage();
                    }
                }
            };

            function prev() {
                if (!isSolrSearch) {
                    if (searchResApps.length > 0 && searchRunning) {
                        var occurrences = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]];
                        if (searchResCurOcc - 1 < 0) {
                            if (searchResCurPage - 1 < 0) {
                                if (searchResCurApp - 1 >= 0) {
                                    searchResCurApp--;
                                    updateSearchPagePos('prev');
                                    printNewPage();
                                }
                            } else {
                                searchResCurPage--;
                                searchResCurOcc = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]].length - 1;
                                searchViewCurPage = parseInt(searchResPages[searchResCurPage]);
                                printNewPage();
                            }
                        } else {
                            searchResCurOcc--;
                            printCurPage();
                        }
                    }
                } else {
                    if (searchResCurApp - 1 >= 0 && searchRunning) {
                        searchResCurApp--;
                        updateSolrSearchPagePos();
                        printNewPage();
                    }
                }
            };

            function nextPage() {
                if (!searchLastPage && searchRunning) {
                    searchViewCurPage++;
                    printNewPage();
                }
            };

            function prevPage() {
                if (searchViewCurPage - 1 > 0 && searchRunning) {
                    searchViewCurPage--;
                    printNewPage();
                }
            };

            function clearDiv() {
                document.getElementById('div2').innerHTML = '';
                selectedFilePath = "";
                lastPage = false;
            };

            function printCurPage() {
                if (searchViewCurPage != parseInt(searchResPages[searchResCurPage])) {
                    searchViewCurPage = parseInt(searchResPages[searchResCurPage]);
                    printNewPage();
                    return;
                }

                var res = document.getElementById('div2').innerHTML;

                var offset = res.indexOf('<br>', res.indexOf('<br>') + 1) + 4;
                var actualPart = res.substring(offset, res.length).replace(/<br>/g, "\n");
                actualPart = actualPart.replace(/<font style="BACKGROUND-COLOR: yellow">/g, '');
                actualPart = actualPart.replace(/<\/font>/g, '');

                var index = parseInt(searchResult[searchResApps[searchResCurApp]][searchViewCurPage][searchResCurOcc]);
                var curLen = actualPart.length;
                var endIndex = index + searchRequestLength > curLen ? curLen : index + searchRequestLength;

                actualPart = actualPart.substring(0, index) + '<FONT style="BACKGROUND-COLOR: yellow">'
                        + actualPart.substring(index, endIndex) + '</FONT>' + actualPart.substring(endIndex, curLen);

                if (endIndex != curLen) {
                    searchBytesToLightFromPrevPage = 0;
                    searchPageToLightFirstBytes = -1;
                } else {
                    searchBytesToLightFromPrevPage = searchRequestLength - (curLen - index) + 1;
                    searchPageToLightFirstBytes = searchViewCurPage + 1;
                }

                actualPart = replaceStringDelimiters(actualPart);

                document.getElementById('div2').innerHTML = res.substring(0, offset) + actualPart;
            };

            function printNewPage() {
                if (!isSolrSearch) {
                    var path;
                    if (!isGrepOverSolr) {
                        path = reversePath(searchResApps[searchResCurApp]);
                    } else {
                        var app = searchResApps[searchResCurApp];
                        path = reversePath(app.substring(0, app.indexOf('||<>||')));
                    }
                    Ext.Ajax.request({
                        url : loc + '/logtool' ,
                        params : {
                            action : 'getlog',
                            path: path,
                            partToView: (searchViewCurPage - 1) * lineForPage,
                            lines: lineForPage
                        },
                        method: 'GET',
                        success: function (result, request) {
                            var resp = replaceStringDelimiters(result.responseText);
                            eval(resp);
                            var res = response.log.replace(/<br>/g, "\n");
                            var totalLength = parseInt(response.total);
                            var totalPages = parseInt(Math.floor(totalLength / lineForPage)) + 1;
                            searchLastPage = (searchViewCurPage == totalPages)

                            if (searchViewCurPage == parseInt(searchResPages[searchResCurPage])) {
                                var index = parseInt(searchResult[searchResApps[searchResCurApp]][searchViewCurPage][searchResCurOcc]);
                                var curLen = res.length;
                                var endIndex = index + searchRequestLength > curLen ? curLen : index + searchRequestLength;

                                res = res.substring(0, index) + '<FONT style="BACKGROUND-COLOR: yellow">'
                                        + res.substring(index, endIndex) + '</FONT>' + res.substring(endIndex, curLen);

                                res = lightFirstBytes(res);
                                if (endIndex != curLen) {
                                    searchBytesToLightFromPrevPage = 0;
                                    searchPageToLightFirstBytes = -1;
                                } else {
                                    searchBytesToLightFromPrevPage = searchRequestLength - (curLen - index) + 1;
                                    searchPageToLightFirstBytes = searchViewCurPage + 1;
                                }
                            } else {
                                res = lightFirstBytes(res);
                            }
                            res = replaceStringDelimiters(res);

                            var app = searchResApps[searchResCurApp];;
                            if (isGrepOverSolr) {
                                app = app.substring(0, app.indexOf('||<>||'));
                            }

                            res = '<FONT style="BACKGROUND-COLOR: lightblue">' + app
                                    + '</FONT>' + '<br>Page viewed '
                                    + searchViewCurPage + ' from ' + totalPages + '<br>' + res;

                            document.getElementById('div2').innerHTML = res;
                        },
                        failure: function (result, request) {
                            Ext.MessageBox.alert('Failed', result.responseText);
                        }
                    });
                } else {
                    Ext.Ajax.request({
                        url : loc + '/logtool' ,
                        params : {
                            action : 'getlog',
                            path: reversePath(solrSearchOccurrences[searchResCurApp].path),
                            partToView: (searchViewCurPage - 1) * lineForPage,
                            lines: lineForPage
                        },
                        method: 'GET',
                        success: function (result, request) {
                            var resp = replaceStringDelimiters(result.responseText);
                            eval(resp);

                            var pageStartIndex = (searchViewCurPage - 1) * lineForPage;
                            var pageEndIndex = searchViewCurPage * lineForPage;

                            var msgStartIndex = parseInt(solrSearchOccurrences[searchResCurApp].startIndex);
                            var msgEndIndex = msgStartIndex + parseInt(solrSearchOccurrences[searchResCurApp].length);

                            var lightingStartIndex = -1;
                            var lightingEndIndex = -1;

                            if (pageStartIndex >= msgStartIndex && pageEndIndex <= msgEndIndex) {
                                lightingStartIndex = 0;
                                lightingEndIndex = lineForPage;
                            } else if (pageStartIndex < msgStartIndex && pageEndIndex <= msgEndIndex && pageEndIndex > msgStartIndex) {
                                lightingStartIndex = msgStartIndex - pageStartIndex;
                                lightingEndIndex = lineForPage;
                            } else if (pageStartIndex >= msgStartIndex && pageEndIndex > msgEndIndex && pageStartIndex < msgEndIndex) {
                                lightingStartIndex = 0;
                                lightingEndIndex = lineForPage - (pageEndIndex - msgEndIndex);
                            } else if (pageStartIndex < msgStartIndex && pageEndIndex > msgEndIndex) {
                                lightingStartIndex = msgStartIndex - pageStartIndex;
                                lightingEndIndex = lineForPage - (pageEndIndex - msgEndIndex);
                            }

                            var res = response.log.replace(/<br>/g, "\n");
                            var totalLength = parseInt(response.total);
                            var totalPages = parseInt(Math.floor(totalLength / lineForPage)) + 1;
                            searchLastPage = (searchViewCurPage == totalPages)

                            if (lightingStartIndex != -1) {
                                res = res.substring(0, lightingStartIndex) + '<FONT style="BACKGROUND-COLOR: yellow">'
                                        + res.substring(lightingStartIndex, lightingEndIndex) + '</FONT>' + res.substring(lightingEndIndex, res.length);
                            }

                            res = replaceStringDelimiters(res);
                            res = '<FONT style="BACKGROUND-COLOR: lightblue">' + solrSearchOccurrences[searchResCurApp].path  + '</FONT>' + '<br>Page viewed '
                                    + searchViewCurPage + ' from ' + totalPages + '<br>' + res;

                            document.getElementById('div2').innerHTML = res;
                        },
                        failure: function (result, request) {
                            Ext.MessageBox.alert('Failed', result.responseText);
                        }
                    });
                }
            };

            function reversePath(path) {
                var pathSegments = path.split('/');
                var res = '';
                for (i = pathSegments.length - 1; i >= 0; i--) {
                    res += pathSegments[i] + '/';
                }
                return res;
            };

            function lightFirstBytes(text) {
                if (searchBytesToLightFromPrevPage > 0 && searchViewCurPage == searchPageToLightFirstBytes) {
                    return '<FONT style="BACKGROUND-COLOR: yellow">' +
                            text.substring(0, searchBytesToLightFromPrevPage) + '</FONT>'
                            + text.substring(searchBytesToLightFromPrevPage, text.length);
                } else {
                    return text;
                }
            };

            function updateSearchPagePos(val) {
                searchResPages = [];
                for (page in searchResult[searchResApps[searchResCurApp]]) {
                    searchResPages.push(page);
                };
                if (val == 'next') {
                    searchResCurPage = 0;
                } else {
                    searchResCurPage = searchResPages.length - 1;
                }
                searchResPages.sort(function(a, b) {return parseInt(a) > parseInt(b)});
                searchViewCurPage = parseInt(searchResPages[searchResCurPage]);

                if (val == 'next') {
                    searchResCurOcc = 0;
                } else {
                    searchResCurOcc = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]].length - 1;
                }
            };

            function updateSolrSearchPagePos() {
                searchViewCurPage = Math.floor(parseInt(solrSearchOccurrences[searchResCurApp].startIndex)/lineForPage) + 1;
            }

            function getSearchResult(response) {
                var res = new Object();
                if (response.length == 3) {
                    return res;
                }
                pos = 1;
                while (pos < response.length - 1) {
                    res = parseFile(res, response);
                }
                return res;
            };

            function parseFile(obj, response) {
                var index = response.indexOf('={', pos);
                var file = response.substring(pos, index);
                obj[file] = new Object();
                pos = index + 2;
                while (response.charAt(pos) != '}') {
                    obj[file] = parsePage(obj[file], response);
                };
                pos += 2;
                return obj;
            };

            function parsePage(obj, response) {
                if (response.charAt(pos) == ',') {
                    pos += 2;
                };
                var index = response.indexOf('=[', pos);
                var page = response.substring(pos, index);
                pos = index + 2;
                index = response.indexOf(']', pos);
                var occurrences = response.substring(pos, index);
                obj[page] = occurrences.split(', ');
                pos = index + 1;
                return obj;
            };

            function replaceStringDelimiters(text) {
                var pattern = /\r\n|\r|\n/g;
                var new_text = text.replace(pattern, "<br>");
                return new_text;
            };
        },
        failure : function(result, request) {
            Ext.MessageBox.alert('Failed', result.responseText);
        }
    });

    function writeText(pathToLog) {
        var prevViewed = partViewed;
        if (pathToLog == 'prev') {
            pathToLog = selectedFilePath;
            if (partViewed >= lineForPage) {
                partViewed = partViewed - lineForPage;
                lastPage = false;
            } else {
                partViewed = 0;
            }
        }
        if (pathToLog == 'next') {
            pathToLog = selectedFilePath;
            partViewed = partViewed + lineForPage;
        }

        Ext.Ajax.request({
            url : loc + '/logtool',
            params : {
                action : 'getlog',
                path : pathToLog,
                partToView : partViewed,
                lines : lineForPage
            },
            method : 'GET',
            success : function(result, request) {
                var res = replaceStringDelimetr(result.responseText);
                eval(res);
                var countLogs = parseInt(response.total);
                partViewed = parseInt(response.partViewed);
                if (partViewed >= countLogs - lineForPage)
                    {
                    lastPage = true;
                    }
                document.getElementById('div2').innerHTML =
                                (' Page viewed ' + parseInt(Math.ceil(partViewed/lineForPage)) +
                                ' from ' + parseInt(Math.floor(countLogs/
                                lineForPage)) + '<br>' + response.log);
            },
            failure : function(result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    };
    function getLogInJsonIndex(text){
        var ind=0;
        for(var i = 0; i < 11;i++ ){
            ind = text.indexOf("'",ind) +1;
        }
        return ind;
    }
    function replaceStringDelimetr(text) {
        var pattern = /\r\n|\r|\n/g;
        text = text.replace(pattern, "<br>");
        var firstIndexOfLog = getLogInJsonIndex(text);
        var headerText = text.substring(0, firstIndexOfLog);
        var log = text.substring(firstIndexOfLog, text.length -2);
        log = log.replace(/'/g," ");
        log = log.replace(/{/g, "[");
        log = log.replace(/}/g, "]");
        return(headerText + log + "'}");
    }
    var updateLog = function update() {
       if (!searchRunning) {
            if (lastPage == true){
                partViewed = -1;
                writeText(selectedFilePath);
            } else if (selectedFilePath != "") {
                writeText(selectedFilePath);
            }
        }
    }

    Ext.TaskManager.start({
        run: updateLog,
        interval: 5000
    });
});

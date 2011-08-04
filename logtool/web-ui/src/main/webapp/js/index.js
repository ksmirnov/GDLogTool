Ext.onReady(function() {
    var selectedFilePath = "";
    var partViewed;
    var lineForPage = 500;
    var lastPage = false;

    var treeFromRoot;

    var loc = location.href;
    loc = loc.substring(0, loc.lastIndexOf('/'));

    var searchResult;
    var pos;
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
                                            icon : 'extjs/resources/delete.gif',
                                            text: 'Delete',
                                            handler : deleteHandler
                                        },
                                        {
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
                            clearDiv();
                            var searchResApps = [];
                            var searchResPages = [];
                            searchField = new Ext.form.TextField({
                                    id: 'searchValue',
                                    fieldLabel: 'Search request',
                                    width: 489
                                });
                        }
                    }]
                });
                searchWindow.show();
            };

            function doSearch() {
                clearDiv();
                var selModel = treePanel.getSelectionModel();
                var selNodes = selModel.getSelection();
                if (selNodes.length > 0) {
                    var selNode = selNodes[0];
                    var path = getFilePath(selNode);
                    var searchRequest = searchField.getValue();
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
                            searchResult = getSearchResult(result.responseText);

                            searchResApps = [];
                            for (app in searchResult) {
                                searchResApps.push(app);
                            };
                            searchResApps.sort();
                            searchResCurApp = 0;

                            updateSearchPagePos('next');

                            searchResultMsgBox();
                            printNewPage();
                        },
                        failure: function (result, request) {
                            Ext.MessageBox.alert('Failed', result.responseText);
                        }
                    });
                }
            };

            function searchResultMsgBox() {
                var res = '';
                if (searchResApps.length == 0) {
                    res += 'nothing found.';
                } else {
                    for (resApp in searchResult) {
                        res += resApp + ': ' + countOccurrences(resApp) + '<br>';
                    }
                }
                Ext.MessageBox.show({
                        title: 'Search results',
                        msg: res, buttons: Ext.MessageBox.OK
                    });
            };

            function countOccurrences(app) {
                var pages = searchResult[app];
                var res = 0;
                for (page in pages) {
                    res += pages[page].length;
                }
                return res;
            };

            function next() {
                if (searchResApps.length > 0) {
                    var occurrences = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]];
                    if (searchResCurOcc + 1 == occurrences.length) {
                        if (searchResCurPage + 1 == searchResPages.length) {
                            if (searchResCurApp + 1 != searchResApps.length) {
                                searchResCurApp++;
                                updateSearchPagePos('next');
                                //searchViewCurPage = parseInt(searchResPages[searchResCurPage]);
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
            };

            function prev() {
                if (searchResApps.length > 0) {
                    var occurrences = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]];
                    if (searchResCurOcc - 1 < 0) {
                        if (searchResCurPage - 1 < 0) {
                            if (searchResCurApp - 1 >= 0) {
                                searchResCurApp--;
                                updateSearchPagePos('prev');
                                //searchViewCurPage = parseInt(searchResPages[searchResCurPage]);
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
            };

            function nextPage() {
                if (!searchLastPage) {
                    searchViewCurPage++;
                    printNewPage();
                }
            };

            function prevPage() {
                if (searchViewCurPage - 1 > 0) {
                    searchViewCurPage--;
                    printNewPage();
                }
            };

            function clearDiv() {
                document.getElementById('div2').innerHTML = '';
            };

            function printCurPage() {
                var res = document.getElementById('div2').innerHTML;
                res = res.replace(/<font style="BACKGROUND-COLOR: yellow">/g, '');
                res = res.replace(/<\/font>/g, '');
                var offset = res.indexOf('<br>', res.indexOf('<br>') + 1) + 4;
                var actualPart = res.substring(offset, res.length).replace(/<br>/g, "\n");

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
                actualPart = replaceStringDelimiter(actualPart);

                document.getElementById('div2').innerHTML = res.substring(0, offset) + actualPart;
            };

            function printNewPage() {
                Ext.Ajax.request({
                    url : loc + '/logtool' ,
                    params : {
                        action : 'getlog',
                        path: reversePath(searchResApps[searchResCurApp]),
                        partToView: (searchViewCurPage - 1) * lineForPage,
                        lines: lineForPage
                    },
                    method: 'GET',
                    success: function (result, request) {
                        var resp = replaceStringDelimiter(result.responseText);
                        eval(resp);
                        var res = response.log.replace(/<br>/g, "\n");
                        var totalLength = parseInt(response.total);
                        var totalPages = parseInt(Math.floor(totalLength / lineForPage));
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
                        res = replaceStringDelimiter(res);
                        res = searchResApps[searchResCurApp] + '<br>Page viewed '
                                + searchViewCurPage + ' from ' + totalPages + '<br>' + res;

                        document.getElementById('div2').innerHTML = res;
                    },
                    failure: function (result, request) {
                        Ext.MessageBox.alert('Failed', result.responseText);
                    }
                });
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
                searchResCurPage = 0;
                searchResPages.sort(function(a, b) {return parseInt(a) > parseInt(b)});
                searchViewCurPage = parseInt(searchResPages[searchResCurPage]);

                if (val == 'next') {
                    searchResCurOcc = 0;
                } else {
                    searchResCurOcc = searchResult[searchResApps[searchResCurApp]][searchResPages[searchResCurPage]].length - 1;
                }
            };

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
                var res = replaceStringDelimiter(result.responseText);
                eval(res);
                var countLogs = parseInt(response.total);
                partViewed = parseInt(response.partViewed);
                if (partViewed >= countLogs - lineForPage) {
                    lastPage = true;
                }
                document.getElementById('div2').innerHTML =
                                (' Page viewed ' + parseInt(Math.ceil(partViewed / lineForPage)) +
                                ' from ' + parseInt(Math.floor(countLogs / lineForPage)) +
                                '<br>' + response.log);

            },
            failure : function(result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    };
    
    function replaceStringDelimiter(text) {
        var pattern = /\r\n|\r|\n/g;
        var new_text = text.replace(pattern, "<br>");
        return new_text;
    }
    
//    var updateLog = function update() {
//        if (lastPage == true) {
//            partViewed = -1;
//            writeText(selectedFilePath);
//        } else if (selectedFilePath != "") {
//            writeText(selectedFilePath);
//        }
//    }
//
//    Ext.TaskManager.start({
//        run: updateLog,
//        interval: 5000
//    });
});

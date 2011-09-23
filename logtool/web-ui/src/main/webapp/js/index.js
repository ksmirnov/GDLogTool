Ext.onReady(function() {

    var loc = location.href;
    loc = loc.substring(0, loc.lastIndexOf('/'));

    var selectedFilePath = "";
    var partViewed;
    var lineForPage = 2500;
    var lastPage = false;

    var refreshLocked = false;
    var firstBoot = true;
    var secondBoot = false;

    var searchInProgress = false;
    var searchRunning = false;
    var searchResult;
    var pos;
    var searchRootDir;
    var searchRequestLength;
    var searchViewCurPage;
    var searchLastPage = false;
    var searchResApps = [];
    var searchResPages = [];
    var searchResCurApp = 0;
    var searchResCurPage;
    var searchResCurOcc;
    var searchBytesToLightFromPrevPage = 0;
    var searchPageToLightFirstBytes = -1;
    var searchSolrPos = 0;
    var searchLogTotalPages = 0;
    var contentFilter = '';
    var facetFilter = '';
    var filters = {};

    var solrSearchOccurrences = [];
    var isSolrSearch = false;
    var isGrepOverSolr = false;


    function getVariable(varName){
        var arg=location.search.substring(1).split('&');
        var variable="";
        var i;
        for(i=0;i<arg.length;i++){
                if(arg[i].split('=')[0]==varName){
                        if(arg[i].split('=').length>1){
                                variable=arg[i].split('=')[1];
                        }
                        return variable;
                }
        }
        return ""
    }
    
    function delTreeEl() {
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
    }

    function addCustomDate() {
        var node = facetsPanel.getRootNode().findChild('text', 'timestamp');
        node.appendChild({
            text: 'custom',
            leaf: true,
            checked: false
        });
    }

    function refreshTree() {
        treestore.load();
        if(searchRunning == false) {
            if(searchField.getValue()) {
                contentFilter = 'content:' + searchField.getValue();
                var operation = new Ext.data.Operation({
                    action: 'read',
                    page: contentFilter
                });
                facetsStore.load(operation);
            } else {
                facetsStore.load();
            }
        }
    }

    function mergeLogs() {
        Ext.MessageBox.alert('Merge', 'Not supported yet!');
    }

    function getFilePath(record) {
        var selectedPath = record.get('text');
        node = record.parentNode;
        while (!node.isRoot()) {
            selectedPath = selectedPath + '/'
                    + node.get('text');
            node = node.parentNode;
        }
        return selectedPath;
    }

    function deleteNode(selNode) {
        var selectedPath = getFilePath(selNode);
        if (selNode.isLeaf()) {
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
        refreshTree();
    }

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
            if (lastPage) {
                partViewed = -1;
            } else {
                partViewed = partViewed + lineForPage;
            }
        }
        if (pathToLog == 'first') {
            pathToLog = selectedFilePath;
            partViewed = 0;
            lastPage = false;
        }
        if (pathToLog == 'last') {
            pathToLog = selectedFilePath;
            partViewed = -1;
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
                var jsonData = Ext.decode(result.responseText);
                if(jsonData.success == true) {
                    var countLogs = parseInt(jsonData.total);
                    partViewed = parseInt(jsonData.partViewed);
                    if (partViewed >= countLogs - lineForPage) {
                        lastPage = true;
                    } else {
                        lastPage = false;
                    }
                    pageNum.setText(parseInt(Math.ceil(partViewed/lineForPage)) +
                            ' / ' + parseInt(Math.floor(countLogs/lineForPage)));
                    display.setValue(jsonData.log);
                    logPagingToolbar.enable();
                } else {
                    clearText();
                    display.setValue('Requested log not found. It is possible the file has been moved or deleted.');
                }
            },
            failure : function(result, request) {
                clearText();
                Ext.MessageBox.alert('Failure', 'Unable to get log!');
            }
        });
    };

    function clearText() {
        display.setValue('Select log to see its content');
        selectedFilePath = "";
        lastPage = false;
        pageNum.setText('0 / 0');
        searchPageNum.setText('0 / 0');
        occurrencesPageNum.setText('0 / 0');
        searchGridStore.removeAll();
        logPagingToolbar.disable();
        display.toggleSourceEdit(true);
    }

    function facetSelected() {
        clearText();
        facetFilter = '';
        filters = {};
        var view = facetsPanel.getView();
        var records = view.getChecked();
        var selected = view.getSelectionModel().getSelection();
        var i;
        var customDate = false;
        for(i = 0; i < records.length; i ++) {
            var text = records[i].get('text');
            var value = text.substr(0, text.lastIndexOf('(') - 1);
            var parentValue = records[i].parentNode.get('text');
            if(!filters[parentValue]) {
                filters[parentValue] = '';
            }
            if(parentValue == 'timestamp') {
                var dt = new Date();
                var currentDt = Ext.Date.format(dt, "Y-m-d") + "T" + Ext.Date.format(dt, "H:i:s") + "Z";
                switch(value) {
                    case 'last hour':
                        dt = Ext.Date.add(dt, Ext.Date.HOUR, -1);
                        break;
                    case 'last day':
                        dt = Ext.Date.add(dt, Ext.Date.DAY, -1);
                        break;
                    case 'last week':
                        dt = Ext.Date.add(dt, Ext.Date.DAY, -7);
                        break;
                    default:
                        if(text == 'custom') {
                            customDate = true;
                        }
                        break;
                }
                if(customDate == false) {
                    var pastDt = Ext.Date.format(dt, "Y-m-d") + "T" + Ext.Date.format(dt, "H:i:s") + "Z";
                    if(filters[parentValue] != '') {
                        filters[parentValue] = filters[parentValue] + ' OR [' + pastDt + ' TO ' + currentDt + ']';
                    } else {
                        filters[parentValue] = '[' + pastDt + ' TO ' + currentDt + ']';
                    }
                }
            } else {
                if(filters[parentValue] != '') {
                    filters[parentValue] = filters[parentValue] + ' OR ' + value;
                } else {
                    filters[parentValue] = value;
                }
            }
        }
        if(customDate == false) {
            for(var key in filters) {
                if(facetFilter != '') {
                    facetFilter = facetFilter + 'AND ';
                }
                facetFilter = facetFilter + key + ':(' + filters[key] + ') ';
            }
            display.toggleSourceEdit(false);
            if(searchField.getValue()) {
                if(facetFilter != '') {
                    doSolrSearch(facetFilter + 'AND content:' + searchField.getValue());
                } else {
                    doSolrSearch('content:' + searchField.getValue());
                }
            } else if (facetFilter){
                doSolrSearch(facetFilter);
            } else {
                facetsPanel.enable();
                searchPagingToolbar.disable();
            }
        } else {
            if(selected[0].get('text') == 'custom') {
                customDateWindow.show();
                return;
            } else {
                submitCustomDate();
            }
        }
    }

    function uncheckCustom() {
        var root = facetsPanel.getRootNode();
        var parent = root.findChildBy(function(n) {
            if (n.get('text') == 'timestamp') {
                return true;
            }
        });
        var leaf = parent.findChildBy(function(n) {
            if (n.get('text') == 'custom') {
                return true;
            }
        });
        leaf.set('checked', false);
    }

    function submitCustomDate() {
        var fromDate = Ext.getCmp('from-date-field');
        var fromTime = Ext.getCmp('from-time-field');
        var toDate = Ext.getCmp('to-date-field');
        var toTime = Ext.getCmp('to-time-field');
        if(fromDate.validate() || fromTime.validate()) {
            if(!fromDate.validate()) {
                fromDate.setValue(toDate.getValue());
            } else {
                fromTime.setValue(toTime.getValue());
            }
            var fromValue = fromDate.getSubmitValue() + 'T' + fromTime.getSubmitValue() + 'Z';
            var toValue = toDate.getSubmitValue() + 'T' + toTime.getSubmitValue() + 'Z';
            if(filters['timestamp'] != '') {
                filters['timestamp'] = filters['timestamp'] + 'OR [' + fromValue + ' TO ' + toValue + ']';
            } else {
                filters['timestamp'] = '[' + fromValue + ' TO ' + toValue + ']';
            }
            for(var key in filters) {
                if(facetFilter != '') {
                    facetFilter = facetFilter + 'AND ';
                }
                facetFilter = facetFilter + key + ':(' + filters[key] + ') ';
            }
            display.toggleSourceEdit(false);
            if(searchField.getValue()) {
                doSolrSearch(facetFilter + 'AND content:' + searchField.getValue());
            } else if (facetFilter){
                doSolrSearch(facetFilter);
            }
            return true;
        } else {
            return false;
        }
    }

    var customDateWindow = Ext.create('Ext.window.Window', {
        title: 'Custom date',
        layout: 'anchor',
        bodyPadding: '5 5 5 5',
        closable: false,
        modal: true,
        items: [
            {
                xtype: 'datefield',
                id: 'from-date-field',
                fieldLabel: 'From date',
                format: 'Y-m-d',
                allowBlank: false,
                editable: false,
                maxValue: new Date(),
                anchor: '100%'
            }, {
                xtype: 'timefield',
                id: 'from-time-field',
                fieldLabel: 'From time',
                format: 'H:i:s',
                allowBlank: false,
                editable: false,
                anchor: '100%'
            }, {
                xtype: 'datefield',
                id: 'to-date-field',
                fieldLabel: 'To date',
                format: 'Y-m-d',
                editable: false,
                maxValue: new Date(),
                value: new Date(),
                anchor: '100%'
            }, {
                xtype: 'timefield',
                id: 'to-time-field',
                fieldLabel: 'To time',
                format: 'H:i:s',
                editable: false,
                value: new Date(),
                anchor: '100%'
            }
        ],
        buttons: [
            {
                text: 'Ok',
                handler: function() {
                    var submitState = submitCustomDate();
                    if(submitState) {
                        customDateWindow.hide();
                    }
                }
            },
            {
                text: 'Cancel',
                handler: function() {
                    Ext.getCmp('from-date-field').reset();
                    Ext.getCmp('from-time-field').reset();
                    customDateWindow.hide();
                    uncheckCustom();
                    facetsPanel.enable();
                }
            }
        ]
    })

    var updateLog = function update() {
       if (!refreshLocked) {
            if (lastPage == true){
                partViewed = -1;
                writeText(selectedFilePath);
            } else if (selectedFilePath != "") {
                writeText(selectedFilePath);
            }
        }
    }
    
    var treestore = Ext.create('Ext.data.TreeStore', {
        proxy: new Ext.data.HttpProxy({
            url: loc + '/logtool?action=gettree'
        }),
        sorters: [
            {
                property: 'leaf',
                direction: 'ASC'
            },
            {
                property: 'text',
                direction: 'ASC'
            }
        ]
    });

    var facetProxy = Ext.create('Ext.data.proxy.Ajax', {
        url: loc + '/logtool?action=doSolrSearch&subaction=getFacets',
        pageParam: 'filter'
    });

    var facetsStore = Ext.create('Ext.data.TreeStore', {
        proxy: facetProxy,
        autoSync: true,
        sorters: [
            {
                property: 'leaf',
                direction: 'ASC'
            },
            {
                property: 'text',
                direction: 'ASC'
            }
        ],
        listeners: {
            load: function(){
                    addCustomDate();
                    setChecked();
                  }
        }
    });


    function setChecked() {
        if (firstBoot == true && (getVariable("content")!= ""  || getVariable("facet") != "")){
            var contentSelected = getVariable("content"); 
            searchField.setValue(contentSelected);
            searchResCurApp = parseInt(getVariable("current"));
            var ord = getVariable("ord");
            if(ord == "asc" || ord == "desc"){
                sortOrderCombo.setValue(ord);
            }
            var ordField = getVariable("ordfield");
            if(ordField == "timestamp" || ordField == "host" || ordField == "instance" || ordField == "application" || ordField == "level"){
                sortFieldCombo.setValue(ordField);
            }
            var facetSelected = getVariable("facet");
            if(facetSelected){
                var searchQuery = facetSelected.replace(/%20TO%20/g,"||").replace(/%20OR%20/g,"||").replace(/%20/g," ").split(' ');
                var i;
                for(i=0;i<searchQuery.length;i++){
                    if(searchQuery[i].split(':')[1].split("||").length > 1){
                        var j;
                        for(j=0;j<searchQuery[i].split(':')[1].split("||").length;j++){
                            setOneChecked(searchQuery[i].split(':')[0],searchQuery[i].split(':')[1].split("||")[j]);
                        }
                    } else {
                        if(searchQuery[i].split(':')[0] != "timestamp"){
                            setOneChecked(searchQuery[i].split(':')[0],searchQuery[i].split(':')[1]);
                        } else {
                            setOneChecked(searchQuery[i].split(':')[0], "custom");
                        }
                    }
                }
            }
            if(contentSelected != "") {
                if(facetSelected != "") {
                    doSolrSearch(getVariable("facet").replace(/%20/g," ") + 'AND content:' + searchField.getValue());
                } else {
                    doSolrSearch('content:' + searchField.getValue());
                }
                contentFilter = 'content:' + searchField.getValue();
                var operation = new Ext.data.Operation({
                    action: 'read',
                    page: contentFilter
                });
                facetsStore.load(operation);
            } else {
                doSolrSearch(getVariable("facet").replace(/%20/g," "));
            }
            if(contentSelected != ""){
                secondBoot = true;
            }
        } else if (secondBoot){
            secondBoot = false;
            var searchQuery = getVariable("facet").replace(/%20OR%20/g,"||").replace(/%20/g," ").split(' ');
            var i;
            for(i=0;i<searchQuery.length;i++){
                if(searchQuery[i].split(':')[1].split("||").length > 1){
                    var j;
                    for(j=0;j<searchQuery[i].split(':')[1].split("||").length;j++){
                        setOneChecked(searchQuery[i].split(':')[0],searchQuery[i].split(':')[1].split("||")[j]);
                    }
                } else {
                    if(searchQuery[i].split(':')[0] != "timestamp"){
                        setOneChecked(searchQuery[i].split(':')[0],searchQuery[i].split(':')[1]);
                    } else {
                        setOneChecked(searchQuery[i].split(':')[0], "custom");
                    }
                }
            }
        }
    }

    function setOneChecked(arg, value){
                var root = facetsPanel.getRootNode();
                var parent = root.findChildBy(function(n) {
                    if (n.get('text') == arg) {
                        return true;
                    }
                });
                var leaf = parent.findChildBy(function(n) {
                    var newOne = value.replace("(","").replace(")","");
                    if (n.get('text').indexOf(value.replace("(","").replace(")","")) != -1) {
                        return true;
                }
                });
                leaf.set('checked', true);
    }
    
    var contextMenu = new Ext.menu.Menu({
        items : [
            {
                text : 'Delete',
                handler : delTreeEl
            }
        ]
    });

    var treePanel = Ext.create('Ext.tree.Panel', {
        id: 'tree-panel',
        title: 'Structure',
        region:'north',
        split: true,
        minHeight: 250,
        height: '50%',
        rootVisible: false,
        autoScroll: true,
        collapsible: true,
        store : treestore,
        listeners : {
            itemclick : function(view, record, item, index, e) {
                var chkNodes = this.getView().getChecked();
                var deleteButton = Ext.getCmp('delete-button');
                if(chkNodes.length > 0) {
                    deleteButton.enable();
                } else {
                    deleteButton.disable();
                }
                if (record.isLeaf()) {
                    clearText();
                    searchField.reset();
                    if(searchRunning == true) {
                        contentFilter = '';
                        facetsStore.load();
                        searchRunning = false;
                    }
                    refreshLocked = false;
                    selectedFilePath = getFilePath(record);
                    partViewed = -1;
                    writeText(selectedFilePath);
                }
            },
            itemcontextmenu : function(view, record, item, index, e) {
                e.stopEvent();
                contextMenu.showAt(e.getXY());
            },
            beforecollapse: function() {
                facetsPanel.expand();
            }
        },
        dockedItems : [
            {
                xtype : 'toolbar',
                items : [
                    'Actions:',
                    '->',
                    {
                        icon: 'extjs/resources/themes/images/default/grid/refresh.gif',
                        text: 'refresh',
                        handler: refreshTree
                    },
//                    {
//                        icon: 'extjs/resources/themes/images/default/grid/columns.gif',
//                        text: 'merge',
//                        handler: mergeLogs
//                    },
                    {
                        id: 'delete-button',
                        icon: 'extjs/resources/delete.gif',
                        text: 'delete',
                        handler : delTreeEl
                    }
                ]
            }
        ]
    });

    var changeTask = new Ext.util.DelayedTask(function(){
        doSearch();
        searchField.focus(false, true);
    });

    var searchField = Ext.create('Ext.form.field.Text', {
        id: 'search-field',
        emptyText: 'Search by content...',
        columnWidth: .8
    });

    var searchBtn = Ext.create('Ext.button.Button', {
        text: 'Search',
        icon: 'thirdparty/magnifier.png',
        columnWidth: .2,
        listeners: {
            click: doSearch
        }
    });

    var facetsPanel = Ext.create('Ext.tree.Panel', {
        id: 'facets-panel',
        title: 'Search navigation',
        region:'center',
        minHeight: 250,
        height: '50%',
        rootVisible: false,
        autoScroll: true,
        collapsible: true,
        store : facetsStore,
        dockedItems : [
            {
                xtype : 'toolbar',
                layout: 'column',
                padding: '5 0 2 5',
                items: [searchField, searchBtn]
            }
        ],
        listeners: {
            beforecollapse: function() {
                treePanel.expand();
            },
            checkchange: function() {
                this.disable();
                facetSelected();
            }
        }
    });

    var display = Ext.create('Ext.form.field.HtmlEditor', {
        id: 'content-area',
        layout: 'fit',
        anchor: '100%',
        border: false,
        autoScroll: true,
        readOnly: true,
        fieldStyle: 'word-wrap: break-word;',
        value: 'Select log to see its content',

        enableAlignments: false,
        enableColors: false,
        enableFont: false,
        enableFontSize: false,
        enableFormat: false,
        enableLinks: false,
        enableLists: false,
        enableSourceEdit: true,
        listeners: {
            activate: function() {
                if ( firstBoot & getVariable("log")!= ""  & getVariable("page") != "") {
                    firstBoot = false;
                    selectedFilePath = getVariable("log").replace(/%2F/g,"/").replace(/%20/g, " ");
                    partViewed = parseInt(getVariable("page"));
                    if(selectedFilePath != "" & partViewed > -1) {
                        writeText(selectedFilePath);
                    }
                }
            }
        }
    });

    var pageNum = Ext.create('Ext.toolbar.TextItem', {
        id: 'page-num',
        text: '0 / 0'
    });

    var logPagingToolbar = Ext.create('Ext.toolbar.Toolbar', {
        id: 'log-paging-toolbar',
        items : [
            '->',
            {
                xtype: 'button',
                icon: 'extjs/resources/themes/images/default/grid/page-first.gif',
                handler: function() {
                    if (!searchRunning) {
                        writeText('first');
                    } else {
                        searchViewCurPage = 1;
                        printNewPage();
                    }
                }
            },
            {
                icon: 'extjs/resources/themes/images/default/grid/page-prev.gif',
                handler: function() {
                    if (!searchRunning) {
                        writeText('prev');
                    } else {
                        if (searchViewCurPage - 1 > 0) {
                            searchViewCurPage--;
                            printNewPage();
                        }
                    }
                }
            }, pageNum,
            {
                icon: 'extjs/resources/themes/images/default/grid/page-next.gif',
                handler: function() {
                    if (!searchRunning) {
                        writeText('next');
                    } else {
                        if (!searchLastPage) {
                            searchViewCurPage++;
                            printNewPage();
                        }
                    }
                }
            },
            {
                icon: 'extjs/resources/themes/images/default/grid/page-last.gif',
                handler: function() {
                    if (!searchRunning) {
                        writeText('last');
                    } else {
                        searchViewCurPage = searchLogTotalPages;
                        printNewPage();
                    }
                }
            },'->',
            {
                text: 'Close',
                handler: clearText
            }
        ]
    });

    var contentPanel = Ext.create('Ext.Panel', {
        id: 'content-panel',
        title: 'Viewport',
        region: 'center',
        layout: 'card',
        margins: '0 0 0 0',
        border: false,
        split: true,
        minHeight: 500,
        minWidth: 200,
        width: '50%',
        collapsible: false,
        items: [display],
        dockedItems : [logPagingToolbar]
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
        {header: "Snippet", flex: 1, sortable: false, dataIndex: 'occurrences'}
    ];

    var sortFieldData = [{field: 'application'}, {field: 'host'}, {field: 'instance'}, {field: 'timestamp'}, {field: 'level'}];
    var sortFields = [
        {type: 'string', name: 'field'}
    ];

    var sortFieldStore = Ext.create('Ext.data.Store', {
        fields: sortFields,
        data: sortFieldData
    });

    var sortFieldCombo = Ext.create('Ext.form.field.ComboBox', {
        displayField: 'field',
        store: sortFieldStore,
        editable: false,
        value: 'timestamp',
        width: 90,
        listeners: {
            change: function() {
                if(searchRunning) {
                    var query = '';
                    if(facetFilter) {
                        query += facetFilter;
                    }
                    if(searchField.getValue()) {
                        if(query) {
                            query += ' AND ';
                        } else {
                            query += searchField.getValue();
                        }
                    }
                    clearText();
                    display.toggleSourceEdit(false);
                    doSolrSearch(query);
                }
            }
        }
    });

    var sortOrderData = [{order: 'asc'}, {order: 'desc'}];
    var sortOrders = [
        {type: 'string', name: 'order'}
    ];

    var sortOrderStore = Ext.create('Ext.data.Store', {
        fields: sortOrders,
        data: sortOrderData
    });

    var sortOrderCombo = Ext.create('Ext.form.field.ComboBox', {
        displayField: 'order',
        store: sortOrderStore,
        editable: false,
        value: 'desc',
        width: 60,
        listeners: {
            change: function() {
                if(searchRunning) {
                    var query = '';
                    if(facetFilter) {
                        query += facetFilter;
                    }
                    if(searchField.getValue()) {
                        if(query) {
                            query += ' AND ';
                        } else {
                            query += searchField.getValue();
                        }
                    }
                    clearText();
                    display.toggleSourceEdit(false);
                    doSolrSearch(query);
                }
            }
        }
    });

    Ext.QuickTips.init();

    var searchPageNum = Ext.create('Ext.toolbar.TextItem', {
        id: 'seach-page-num',
        text: '0 / 0'
    });

    var occurrencesPageNum = Ext.create('Ext.toolbar.TextItem', {
        id: 'occurrences-page-num',
        text: '0 / 0'
    });

    var searchPagingToolbar = Ext.create('Ext.toolbar.Toolbar', {
        id: 'search-paging-toolbar',
        items : [
            sortFieldCombo, sortOrderCombo,
//            {
//                icon: 'extjs/resources/themes/images/default/grid/page-prev.gif',
//                handler : prevPage,
//                tooltip: 'Previous log page'
//            },
//            {
//                icon: 'extjs/resources/themes/images/default/grid/page-next.gif',
//                handler : nextPage,
//                tooltip: 'Next log page'
//            },
            {
                icon: 'extjs/resources/themes/images/default/grid/page-prev.gif',
                handler : prev,
                tooltip: 'Previous occurrence'
            }, occurrencesPageNum,
            {
                icon: 'extjs/resources/themes/images/default/grid/page-next.gif',
                handler : next,
                tooltip: 'Next occurrence'
            },
            {
                icon: 'extjs/resources/themes/images/default/grid/page-prev.gif',
                handler : prevSearchPage,
                tooltip: 'Previous search page'
            }, searchPageNum,
            {
                icon: 'extjs/resources/themes/images/default/grid/page-next.gif',
                handler : nextSearchPage,
                tooltip: 'Next search page'
            },
            '->',
            {
                text: 'Cancel',
                handler: onCancel
            }
        ]
    });

    var searchResultsPanel = Ext.create('Ext.grid.Panel', {
        id: 'search-results-panel',
        title: 'Search results',
        region: 'north',
        layout: 'card',
        margins: '0 0 5 0',
        border: true,
        minHeight: 250,
        height: '99%',
        store: searchGridStore,
        columns: columns,
        listeners: {
            itemdblclick: function(dataView, record, item, index, e) {
                searchResCurApp = searchSolrPos + index;
                updateSolrSearchPagePos();
                printNewPage();
            }
        },
        dockedItems: [searchPagingToolbar]
    });

    var linkField = Ext.create('Ext.form.field.Text', {
        id: 'link-field',
        anchor: '100%',
        emptyText: 'Click \'Get link\' to receive url'
    });

    var linkBtn = Ext.create('Ext.button.Button', {
        text: 'Get link',
        anchor: '100%',
        listeners: {
                click: function() {
                    var bookmark;
                    if(selectedFilePath != ""){
                        bookmark = selectedFilePath;
                        bookmark = bookmark.replace(/\//g,"%2F").replace(/ /g,"%20");
                        bookmark = loc + "/?log=" + bookmark + "&page=" + partViewed;
                    } else if (searchRunning) {
                        bookmark = loc + "/?";                    
                        if(facetFilter != ""){
                            bookmark = bookmark + "facet=" + facetFilter.trim().replace(/ /g,"%20").replace();
                        }
                        if(searchField.getValue()){
                            bookmark = bookmark + "&content=" + searchField.getValue();
                        }
                        bookmark = bookmark +  "&current=" + searchResCurApp;
                        bookmark = bookmark + "&ord=" + sortOrderCombo.getValue() + "&ordfield=" + sortFieldCombo.getValue();
                    }
                    linkField.setValue(bookmark);
                    linkField.focus(true, true);
                }
        }
    });

    var alertsBtn = Ext.create('Ext.button.Button', {
        text: 'Check alerts',
        anchor: '100%',
        listeners: {
                click: function() {
                    window.location= loc + "/alerts.html";
                }
        }
    });

    var stuffPanel = Ext.create('Ext.form.Panel', {
        id: 'stuff-panel',
        title: 'Extras',
        region: 'center',
        border: true,
        minHeight: 94,
        maxHeight: 94,
        bodyPadding: 5,
        items: [
            {
                layout: 'column',
                anchor: '0',
                border: false,
                items: [
                    {
                        layout: 'anchor',
                        columnWidth: .7,
                        border: false,
                        items: [linkField]
                    },
                    {
                        layout: 'anchor',
                        columnWidth: .3,
                        bodyPadding: '0 0 0 5',
                        border: false,
                        items: [linkBtn]
                    }
                ]
            },
            {
                tag: 'hr'
            },
            {
                layout: 'column',
                anchor: '0',
                border: false,
                bodyPadding: '5 0 0 0',
                items: [
                    {
                        layout: 'anchor',
                        columnWidth: .7,
                        border: false,
                        items: [
                            {
                                text: ' ',
                                bodyStyle: 'border-color: #FFFFFF;'
                            }
                        ]
                    },
                    {
                        layout: 'anchor',
                        columnWidth: .3,
                        bodyPadding: '0 0 0 5',
                        border: false,
                        items: [alertsBtn]
                    }
                ]
            },
        ]
    });

    var viewport = Ext.create('Ext.Viewport', {
        layout: 'border',
        title: 'GDLogTool',
        items: [
            {
                xtype: 'box',
                id: 'header',
                region: 'north',
                html: '<h1><b> GDLogTool</b></h1>',
                height: 30
            },
            {
                layout: 'border',
                id: 'left-browser',
                region: 'west',
                border: false,
                split: true,
                margins: '5 0 5 5',
                minWidth: 250,
                width: '20%',
                items: [treePanel, facetsPanel]
            },
            {
                layout: 'border',
                id: 'center-browser',
                region: 'center',
                border: true,
                split: true,
                margins: '5 0 5 0',
                minWidth: 350,
                width: "50%",
                items: [contentPanel]
            },
            {
                layout: 'border',
                id: 'right-browser',
                region: 'east',
                border: false,
                split: true,
                margins: '5 5 5 0',
                minWidth: 250,
                width: "30%",
                items: [searchResultsPanel, stuffPanel]
            }
        ]
    });

    viewport.render(Ext.getBody());
    logPagingToolbar.disable();
    searchPagingToolbar.disable();
    Ext.getCmp('delete-button').disable();
    
    Ext.TaskManager.start({
        run: updateLog,
        interval: 5000
    });

    Ext.get('search-field').on('keyup', function(){
        changeTask.delay(1000);
    });

    function updateSearchPageNum() {
        searchPageNum.setText(parseInt(Math.ceil((searchSolrPos + 1)/10)) +
                            ' / ' + parseInt(Math.ceil(solrSearchOccurrences.length/10)));
    }

    function updateOccurrencesPageNum() {
        occurrencesPageNum.setText( (searchResCurApp + 1) +
                            ' / ' + solrSearchOccurrences.length);
    }

    function updateNums() {
        updateSearchPageNum();
        updateOccurrencesPageNum();
    }

    function prevSearchPage() {
        if (searchSolrPos > 9) {
            searchPagingToolbar.disable();
            searchSolrPos = searchSolrPos - 10;
            searchGridStore.removeAll();
            updateNums();
            addOneRow(0);
            searchResCurApp = searchResCurApp -1 - searchResCurApp % 10;
            updateSolrSearchPagePos();
            printNewPage();
        }
    }

    function nextSearchPage() {
        if (searchSolrPos < solrSearchOccurrences.length - 10) {
            searchPagingToolbar.disable();
            searchSolrPos = searchSolrPos + 10;
            searchGridStore.removeAll();
            updateNums();
            addOneRow(0);
            searchResCurApp += (10 - searchResCurApp % 10);
            updateSolrSearchPagePos();
            printNewPage();
        }
    }

    function onCancel() {
        searchRunning = false;
        searchResApps = [];
        searchResPages = [];
        solrSearchOccurrences = [];
        searchField.setValue('');
        clearText();
        searchPagingToolbar.disable();
        facetsStore.load();
    };

    function doSearch() {
        clearText();
        display.toggleSourceEdit(false);
        if(searchField.getValue()) {
            contentFilter = 'content:' + searchField.getValue();
            var operation = new Ext.data.Operation({
                action: 'read',
                page: contentFilter
            });
            facetsStore.load(operation);
            doSolrSearch(contentFilter);
        } else {
            onCancel();
        }

    };

    function doSolrSearch(query) {
        if(!searchInProgress){
            searchBtn.disable();
            searchInProgress = true;
            searchRunning = true;

            Ext.Ajax.request({
                url : loc + '/logtool' ,
                params : {
                    action: 'doSolrSearch',
                    subaction: 'solrsearch',
                    query: query,
                    start: 0,
                    amount: 300,
                    sortField: sortFieldCombo.getValue(),
                    order: sortOrderCombo.getValue()
                },
                method: 'GET',
                success: function (result, request) {
                    solrSearchOccurrences = Ext.decode(result.responseText);

                    if (solrSearchOccurrences.length == 0) {
                        display.setValue('Nothing found.');
                        facetsPanel.enable();
                        return;
                    }
                    addSearchResultToGrid();
                    if(firstBoot == true){
                        firstBoot = false;
                    } else {
                        searchResCurApp = 0;
                    }
                    updateSolrSearchPagePos();

                    logPagingToolbar.enable();

                    printNewPage();
                    facetsPanel.enable();
                },
                failure: function (result, request) {
                    Ext.MessageBox.alert('Failed', result.responseText);
                    facetsPanel.enable();
                }
            });
        }
    };

    function addSearchResultToGrid() {
        addOneRow(0);
    };

    function addOneRow(index) {
        var curIndex = searchSolrPos + index;
        if (index == 10 || solrSearchOccurrences.length == curIndex) {
            searchPagingToolbar.enable();
            searchBtn.enable();
            searchResultsPanel.getView().select(searchResCurApp % 10);
            searchInProgress = false;
            return;
        }

        var length = 100;

        if (parseInt(solrSearchOccurrences[curIndex].length) < 100) {
            length = solrSearchOccurrences[curIndex].length;
        }
        Ext.Ajax.request({
            url : loc + '/logtool' ,
            params : {
                action : 'getLog',
                path: reversePath(solrSearchOccurrences[curIndex].path),
                partToView: solrSearchOccurrences[curIndex].startIndex,
                lines: length
            },
            method: 'GET',
            success: function (result, request) {
                var jsonData = Ext.decode(result.responseText);
                var appspec = solrSearchOccurrences[curIndex].application + ' / ' +
                        solrSearchOccurrences[curIndex].host + ' / ' +
                        solrSearchOccurrences[curIndex].instance + ' / ' +
                        solrSearchOccurrences[curIndex].timestamp +
                        '    (' + solrSearchOccurrences[curIndex].startIndex + ', ' + solrSearchOccurrences[curIndex].length + ')';
                var msg = jsonData.log;
                if (msg.length == 100) {
                    msg += ' ...';
                }
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

    function next() {
        if (searchResCurApp + 1 != solrSearchOccurrences.length && searchRunning) {
            if((searchResCurApp + 1) % 10 == 0) {
                nextSearchPage();
            } else {
                searchResCurApp++;
                updateSolrSearchPagePos();
                printNewPage();
                searchResultsPanel.getView().select(searchResCurApp % 10);
            }
        }
    };

    function prev() {
        if (searchResCurApp - 1 >= 0 && searchRunning) {
            if(searchResCurApp % 10 == 0) {
                prevSearchPage();
            } else {
                searchResCurApp--;
                updateSolrSearchPagePos();
                printNewPage();
                searchResultsPanel.getView().select(searchResCurApp % 10);
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

    function printNewPage() {
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
                var jsonData = Ext.decode(result.responseText);

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

                var res = (replaceStringDelimiters(jsonData.log)).replace(/<br>/g, "\n");
                var totalLength = parseInt(jsonData.total);
                var totalPages = parseInt(Math.floor(totalLength / lineForPage)) + 1;
                searchLastPage = (searchViewCurPage == totalPages)

                if (lightingStartIndex != -1) {
                    res = res.substring(0, lightingStartIndex) + '<FONT style="BACKGROUND-COLOR: yellow">'
                            + res.substring(lightingStartIndex, lightingEndIndex) + '</FONT>' + res.substring(lightingEndIndex, res.length);
                }

                res = replaceStringDelimiters(res);
                res = '<FONT style="BACKGROUND-COLOR: lightblue">' + solrSearchOccurrences[searchResCurApp].path  + '</FONT>'
                    //+ '<br>Page viewed '+ searchViewCurPage + ' from ' + totalPages
                    + '<br>' + res;

                searchLogTotalPages = totalPages;
                pageNum.setText(searchViewCurPage + ' / ' + totalPages);

                display.setValue(res);

                updateNums();
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

    function updateSolrSearchPagePos() {
        searchViewCurPage = Math.floor(parseInt(solrSearchOccurrences[searchResCurApp].startIndex)/lineForPage) + 1;
    }

    function replaceStringDelimiters(text) {
        var pattern = /\\r\\n|\\r|\\n|\r\n|\r|\n/g;
        var new_text = text.replace(pattern, "<br>");
        return new_text;
    };
    
});
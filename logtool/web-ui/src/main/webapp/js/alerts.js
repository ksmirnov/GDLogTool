Ext.require([
    'Ext.form.*',
    'Ext.layout.container.Column',
    'Ext.tab.Panel'
]);

Ext.onReady(function(){

    var loc = location.href;
    loc = loc.substring(0, loc.lastIndexOf('/'));

    Ext.QuickTips.init();

    var filtersStore = Ext.create('Ext.data.ArrayStore', {
        fields: [
           {name: 'filter'}
        ]
    });

    var alertsStore = Ext.create('Ext.data.ArrayStore', {
        fields: [
           {name: 'alert'},
           {name: 'full'}
        ]
    });

    var emailsStore = Ext.create('Ext.data.ArrayStore', {
        fields: [
           {name: 'email'}
        ]
    });

    function getFilters(store) {
        Ext.Ajax.request({
            url : loc + '/logtool',
            params : {
            	action: 'alertsAction',
                subaction: 'getFilters'
            },
            method: 'GET',
            success: function (result, request) {
                store.loadData(Ext.JSON.decode(result.responseText));
            },
            failure: function (result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    }

    function getEmailAddresses(store, filter) {
        Ext.Ajax.request({
            url : loc + '/logtool',
            params : {
            	action: 'alertsAction',
                subaction: 'getEmailAddresses',
                filter: filter
            },
            method: 'GET',
            success: function (result, request) {
                store.loadData(Ext.JSON.decode(result.responseText));
            },
            failure: function (result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    }

    function getAlerts(store, filter) {
        Ext.Ajax.request({
            url : loc + '/logtool',
            params : {
            	action: 'alertsAction',
                subaction: 'getAlerts',
                filter: filter
            },
            method: 'GET',
            success: function (result, request) {
                store.loadData(Ext.JSON.decode(result.responseText));
            },
            failure: function (result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    }

    function removeAlert(store, filter, message) {
        Ext.Ajax.request({
            url : loc + '/logtool',
            params : {
            	action: 'alertsAction',
                subaction: 'removeAlert',
                filter: filter,
                message: message
            },
            method: 'GET',
            success: function (result, request) {
                store.loadData(Ext.JSON.decode(result.responseText));
            },
            failure: function (result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    }

    function removeFilter(store, filter) {
        Ext.Ajax.request({
            url : loc + '/logtool',
            params : {
            	action: 'alertsAction',
                subaction: 'removeFilter',
                filter: filter
            },
            method: 'GET',
            success: function (result, request) {
                store.loadData(Ext.JSON.decode(result.responseText));
            },
            failure: function (result, request) {
                Ext.MessageBox.alert('Failed', result.responseText);
            }
        });
    }

    var emailsGrid = Ext.create('Ext.grid.Panel', {
        store: emailsStore,
        stateful: true,
        stateId: 'stateGrid',
        height: 300,
        width: '100%',
        columns: [
            {
                text     : 'Email address',
                sortable : true,
                flex: 1,
                dataIndex: 'email'
            }, {
                xtype: 'actioncolumn',
                width: 25,
                items: [{
            		icon: 'extjs/resources/delete.gif',
            		handler: function(grid, rowIndex, colIndex) {
                        alert('Not supported yet!');
                    }
                }]
            }
        ],
        viewConfig: {
            stripeRows: true
        }
    });

    var emailField = Ext.create('Ext.form.field.Text', {
        fieldLabel: 'E-mail',
        anchor: '100%'
    });

    var addEmailBtn = Ext.create('Ext.Button', {
        text: 'Add',
        handler: function() {
            alert('Not supported yet!');
        }
    });

    var subscribersWnd = Ext.create('Ext.window.Window', {
        title: 'Subscribers list',
        height: 400,
        width: 400,
        layout: 'anchor',
        bodyStyle:'padding: 5',
        items: [
            emailsGrid,
            emailField
        ],
        buttons: [
        	addEmailBtn,
        	{
	            text: 'Close',
	            handler: function() {
	                subscribersWnd.hide();
	            }
        	}
        ]
    });

	var filtersGrid = Ext.create('Ext.grid.Panel', {
        store: filtersStore,
        stateful: true,
        stateId: 'stateGrid',
        height: '100%',
        width: '100%',
        columns: [
            {
                text     : 'Filter',
                sortable : true,
                flex     : 1,
                dataIndex: 'filter'
            },
            {
            	xtype: 'actioncolumn',
            	width: 25,
            	items: [{
            		icon: 'extjs/resources/delete.gif',
            		handler: function(grid, rowIndex, colIndex) {
                        filter = filtersStore.getAt(rowIndex).get('filter');
                        Ext.MessageBox.confirm('Delete prompt',
                                'Are you sure you want to delete filter `' + filter + '`?',
                                function(btn) {
                                    if(btn=='yes') {
                                        removeFilter(filtersStore, filter);
                                    }
                                });
            		}
            	}]
            }
        ],
        viewConfig: {
            stripeRows: true
        },
        listeners : {
        	afterrender: function() {
                getFilters(filtersStore);
            },
            itemclick: function(dataView, record, item, index, e) {
                getAlerts(alertsStore, filtersStore.getAt(index).get('filter'));
            },
            itemdblclick: function(dataView, record, item, index, e) {
                getEmailAddresses(emailsStore, filtersStore.getAt(index).get('filter'));
                subscribersWnd.show();
            }
        }
    });

	var alertsGrid = Ext.create('Ext.grid.Panel', {
        store: alertsStore,
        stateful: true,
        stateId: 'stateGrid',
        height: '100%',
        width: '100%',
        hideHeaders: true,
        columns: [{
            sortable : true,
            flex: 1,
            dataIndex: 'alert'
        }],
        viewConfig: {
            stripeRows: true
        }, listeners: {
            itemdblclick: function(dataView, record, item, index, e) {
                selAlert = alertsStore.getAt(index).get('full');
                selRow = filtersGrid.getSelectionModel().getSelection();
                var alertMessage = Ext.create('Ext.window.Window', {
                    title: 'Alert message',
                    height: 200,
                    width: 400,
                    layout: 'fit',
                    items: {
                        xtype: 'textarea',
                        anchor: '100%',
                        value: selAlert
                    },
                    buttons: [{
                        text: 'Close and remove',
                        handler: function() {
                            removeAlert(alertsStore, selRow[0].get('filter'), selAlert);
                            alertMessage.hide();
                            alertMessage.destroy();
                        }
                    }, {
                        text: 'Cancel',
                        handler: function() {
                            alertMessage.hide();
                            alertMessage.destroy();
                        }
                    }]
                }).show();
            }
        }
    });

    var alertsPanel = Ext.create('Ext.form.Panel', {
        frame:true,
        bodyStyle:'padding: 0',
        width: '100%',
        height: '100%',
        layout:'column',
        
        items: [{
		    title: 'Filters',
		    columnWidth: 2/5,
            items: filtersGrid
		},{
		    title: 'Items',
		    columnWidth: 3/5,
		    items: alertsGrid
		}],
		dockedItems : [{
			xtype : 'toolbar',
			items : [
			'<b style = "font-size: 16; color: black">GDLogTool alerts</b>',
			'->', 
				{
					icon : 'extjs/resources/themes/images/default/shared/left-btn.gif',
					text: '<b style = "font-size: 12; color: black">Logs</b>',
					handler : function() {
				        window.location = loc + "/";
				    }
				}
			]
		}]
    });

    alertsPanel.render(document.getElementById("content"));

});

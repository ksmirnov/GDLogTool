Ext.onReady(function() {
	
	var selectedFilePath;
	var partViewed;
	var lineForPage = 30;

	var treeFromRoot;

	var loc = location.href;
	loc = loc.substring(0, loc.lastIndexOf('/'));
	
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
										}
									]
								}]
					});

			var map = new Ext.KeyMap(Ext.get('treePanel'), {
						key : [Ext.EventObject.DELETE,
								Ext.EventObject.BACKSPACE],
						fn : deleteHandler
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
		},
		failure : function(result, request) {
			Ext.MessageBox.alert('Failed', result.responseText);
		}
	});

	function writeText(pathToLog) {
		var prevViewed = partViewed;
		if (pathToLog == 'prev') {
			pathToLog = selectedFilePath;
			if (partViewed - lineForPage > lineForPage) {
				partViewed = partViewed - lineForPage;
			} else {
				partViewed = lineForPage;
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
				eval(result.responseText);
				var countLogs = parseInt(response.total);
				partViewed = parseInt(response.partViewed);
				document.getElementById('div2').innerHTML = (' Page viewed '
						+ parseInt(partViewed / lineForPage) + ' from '
						+ parseInt(countLogs / lineForPage) + '<br>' + response.log);
			},
			failure : function(result, request) {
				Ext.MessageBox.alert('Failed', result.responseText);
			}
		});
	};
});

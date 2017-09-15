'use struct'

var RE = {
	currentRange: {
		startContainer: null,
		startOffset: 0,
		endContainer: null,
		endOffset: 0
	},
	cache: {
		editor: null,
		title: null,
		currentLink: null
	},
	commandSet: ['bold', 'italic', 'strikethrough', 'redo', 'undo'],
	schemeCache: {
		FOCUS_SCHEME: 'focus://',
		CHANGE_SCHEME: 'change://',
		STATE_SCHEME: 'state://',
		CALLBACK_SCHEME: 'callback://',
		IMAGE_SCHEME: 'image://'
	},
	setting: {
		screenWidth: 0,
		margin: 20
	},
	imageCache: new Map(),
	init: function(){    //初始化内部变量
		const _self = this;
		_self.initCache();
		_self.initSetting();
		_self.bind();
	},
	bind: function(){
		const _self = this;

		const { FOCUS_SCHEME, STATE_SCHEME, CALLBACK_SCHEME } = _self.schemeCache;

		document.addEventListener('selectionchange', _self.saveRange, false);

		_self.cache.title.addEventListener('focus', function(){
			AndroidInterface.setViewEnabled(true);
		}, false);

		_self.cache.title.addEventListener('blur', () => {
			AndroidInterface.setViewEnabled(false);
		}, false);

		_self.cache.editor.addEventListener('blur', () => {
			_self.saveRange();
		}, false);

		_self.cache.editor.addEventListener('click', (evt) => {
			_self.saveRange();
			_self.getEditItem(evt);
		}, false);

		
		_self.cache.editor.addEventListener('keyup', (evt) => {
			if(evt.which == 37 || evt.which == 39 || evt.which == 13 || evt.which == 8){
				_self.getEditItem(evt);
			}
		}, false);

		_self.cache.editor.addEventListener('input', () => {
			AndroidInterface.staticWords(_self.staticWords());
		}, false);
	},
	initCache: function(){
		const _self = this;
		_self.cache.editor = document.getElementById('editor');
		_self.cache.title = document.getElementById('title');
		_self.cache.editor.style.minHeight = window.innerHeight - 69 + 'px';
	},
	initSetting: function(){
		const _self = this;
		_self.setting.screenWidth = window.innerWidth - 20;
	},
	focus: function(){   //聚焦
		const _self = this;
		const range = document.createRange();
		range.selectNodeContents(this.cache.editor);
		range.collapse(false);
		const select = window.getSelection();
		select.removeAllRanges();
		select.addRange(range);
		_self.cache.editor.focus();
	},
	getHtml: function(){
		const _self = this;
		return _self.cache.editor.innerHTML;
	},
	staticWords: function(){
		const _self = this;
		return _self.cache.editor.innerHTML.replace(/<div\sclass="tips">.*<\/div>|<\/?[^>]*>/g, '').trim().length;
	},
	saveRange: function(){   //保存节点位置
		const _self = this;
		const selection = window.getSelection();
		if(selection.rangeCount > 0){
			const range = selection.getRangeAt(0);
			const { startContainer, startOffset, endContainer, endOffset} = range;
			_self.currentRange = {
				startContainer: startContainer,
				startOffset: startOffset,
				endContainer: endContainer,
				endOffset: endOffset
			};
		}
	},
	reduceRange: function(){  //还原节点位置
		const _self = this;
		const { startContainer, startOffset, endContainer, endOffset} = _self.currentRange;
		const range = document.createRange();
		const selection = window.getSelection();
		selection.removeAllRanges();
		range.setStart(startContainer, startOffset);
		range.setEnd(endContainer, endOffset);
		selection.addRange(range);
	},
	exec: function(command){    //执行指令
		const _self = this;
		if(_self.commandSet.indexOf(command) !== -1){
			document.execCommand(command, false, null);
		}else{
			let value = '<'+command+'>';
			document.execCommand('formatBlock', false, value);
		}
	},
	getEditItem: function(evt){      //通过点击时，去获得一个当前位置的所有状态
		const _self = this;
		const { STATE_SCHEME, CHANGE_SCHEME, IMAGE_SCHEME } = _self.schemeCache;
		if(evt.target && evt.target.tagName === 'A'){
			_self.cache.currentLink = evt.target;
			const name = evt.target.innerText;
			const href = evt.target.getAttribute('href');
			window.location.href = CHANGE_SCHEME + encodeURI(name + '@_@' + href);
		}else{
			if(e.which == 8){
				AndroidInterface.staticWords(_self.staticWords());
			}
			const items = [];
			_self.commandSet.forEach((item) => {
				if(document.queryCommandState(item)){
					items.push(item);
				}
			});
			if(document.queryCommandValue('formatBlock')){
				items.push(document.queryCommandValue('formatBlock'));
			}
			window.location.href = STATE_SCHEME + encodeURI(items.join(','));
		}
	},
	insertHtml: function(html){
		const _self = this;
		document.execCommand('insertHtml', false, html);
	},
	insertLine: function(){
		const _self = this;
		const html = '<hr><div><br></div>';
		_self.insertHtml(html);
	},
	insertLink: function(name, url){
		const _self = this;
		const html = `<a href="${url}" class="editor-link">${name}</a>`;
		_self.insertHtml(html);
	},
	changeLink: function(name, url){
		const _self = this;
		const current = _self.cache.currentLink;
		const len = name.length;
		current.innerText = name;
		current.setAttribute('href', url);
		const selection = window.getSelection();
		const range = selection.getRangeAt(0).cloneRange();
		const { startContainer, endContainer } = _self.currentRange;
		selection.removeAllRanges();
		range.setStart(startContainer, len);
		range.setEnd(endContainer, len);
		selection.addRange(range);
	},
	insertImage: function(url, id, width, height){
		const _self = this;
		let newWidth=0, newHeight = 0;
		const { screenWidth } = _self.setting;
		if(width > screenWidth){
			newWidth = screenWidth;
			newHeight = height * newWidth / width;
		}else{
			newWidth = width;
			newHeight = height;
		}
		const image = `<div><br></div><div class="img-block">
				<div style="width: ${newWidth}px" class="process">
					<div class="fill">
					</div>
				</div>
				<img class="images" data-id="${id}" style="width: ${newWidth}px; height: ${newHeight}px;" src="${url}"/>
				<div class="cover" style="width: ${newWidth}px; height: ${newHeight}px"></div>
				<div class="delete">
					<img src="./reload.png">
					<div class="tips">图片上传失败，请点击重试</div>
				</div>
				<input type="text" placeholder="请输入图片名字">
			</div><div><br></div>`;
		_self.insertHtml(image);
		const img = document.querySelector(`img[data-id="${id}"]`);
		const imgBlock = img.parentNode;
		imgBlock.contentEditable = false;
		imgBlock.addEventListener('click', (e) => {
			e.stopPropagation();
			const current = e.currentTarget;
			const img = current.querySelector('.images');
			const id = img.getAttribute('data-id');
			window.location.href = _self.schemeCache.IMAGE_SCHEME + encodeURI(id);
		}, false);
		_self.imageCache.set(id, imgBlock);
	},
	changeProcess: function(id, process){
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		var fill = imgBlock.querySelector('.fill');
		fill.style.width = `${process}%`;
		if(process == 100){
			var cover = imgBlock.querySelector('.cover');
			var process = imgBlock.querySelector('.process');
			imgBlock.removeChild(cover);
			imgBlock.removeChild(process);
		}
	},
	removeImage: function(id){
		var _self = this;
		var imgBlock = _self.imageCache.get(id);
		imgBlock.parentNode.removeChild(imgBlock);
		_self.imageCache.delete(id);
	},
	uploadFailure: function(id){
		const _self = this;
		const imgBlock = _self.imageCache.get(id);
		const del = imgBlock.querySelector('.delete');
		del.style.display = 'block';
		console.log('uploadFailure');
	},
	uploadReload: function(id){
		const _self = this;
		const imgBlock = _self.imageCache.get(id);
		const del = imgBlock.querySelector('.delete');
		del.style.display = 'none';
	}
};

RE.init();
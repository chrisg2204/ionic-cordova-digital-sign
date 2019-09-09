var exec = require('cordova/exec');
var PLUGIN_NAME = 'PDFDigiSign';

var PDFDigiSign = {
	execSign: function (successCallback, errorCallback){
		exec(successCallback, errorCallback, PLUGIN_NAME, "sign");
	}

};

module.exports = PDFDigiSign;
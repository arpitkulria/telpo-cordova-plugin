
function MagneticCardHelper() {
}

// function SmartCardHelper() {
// }

MagneticCardHelper.prototype.open = function(callback) {
    var failureCallback = function() {
        console.log("Problem while open");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "open", []);
};

MagneticCardHelper.prototype.startReading = function(callback) {
    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "startReading", []);
};


MagneticCardHelper.prototype.startMonitor = function(callback) {
    console.log("+++++++++++++++IN START MONITOR++++++++++++++++++++++");
    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "startMonitor", []);
};


MagneticCardHelper.prototype.readSmartCard = function(callback, failureCallback) {
    console.log("+++++++++++++++IN readSmartCard++++++++++++++++++++++");
    cordova.exec(callback, failureCallback, "MagneticCardHelper", "readSmartCard", []);
    cordova.fireDocumentEvent("dataMapEvent");
};


MagneticCardHelper.prototype.stop = function(callback) {
    console.log("+++++++++++++++IN STOP MONITOR++++++++++++++++++++++");
    var failureCallback = function() {
        console.log("Problem while stopping");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "stop", []);
};


MagneticCardHelper.prototype.print = function(content, signaturePath, logo, callback) {
    console.log("+++++++++++++++IN PRINT++++++++++++++++++++ ::: ");
    var failureCallback = function() {
        console.log("Problem while stopping");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "print", [content, signaturePath, logo]);
};


module.exports = new MagneticCardHelper();
// module.exports = new SmartCardHelper();

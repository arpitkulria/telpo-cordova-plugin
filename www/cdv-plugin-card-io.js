
function MagneticCardHelper() {
}

// function SmartCardHelper() {
// }

MagneticCardHelper.prototype.open = function() {
    var failureCallback = function() {
        console.log("Problem while open");
    };

    var wrappedSuccess = function() {
        console.log("Open success");
    };

    cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "open", []);
};

MagneticCardHelper.prototype.startReading = function(callback) {
    var failureCallback = function() {
        console.log("Problem while startReading");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "startReading", []);
};


MagneticCardHelper.prototype.startMonitor = function(callback) {
    console.log("+++++++++++++++IN START MONITOR++++++++++++++++++++++");
    var failureCallbackStop = function() {
        console.log("Problem while Stoping");
    };

    var failureCallback = function() {
        console.log("Problem while startReading");
        cordova.exec(callback, failureCallbackStop, "MagneticCardHelper", "stopMonitor", []);
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "startMonitor", []);
};


module.exports = new MagneticCardHelper();
// module.exports = new SmartCardHelper();


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


MagneticCardHelper.prototype.stop = function(callback) {
    console.log("+++++++++++++++IN STOP MONITOR++++++++++++++++++++++");
    var failureCallback = function() {
        console.log("Problem while stopping");
    };

    cordova.exec(callback, failureCallback, "MagneticCardHelper", "stop", []);
};


module.exports = new MagneticCardHelper();
// module.exports = new SmartCardHelper();

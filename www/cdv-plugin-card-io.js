
function MagneticCardHelper() {

}

MagneticCardHelper.prototype.open = function() {
  console.log("-----------In OPEN FUNCTION---------------------------");
    var failureCallback = function() {
        console.log("Problem while open");
    };

    var wrappedSuccess = function() {
        console.log("Open success");
    };
  cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "open", []);
};


MagneticCardHelper.prototype.startReading = new Promise(function(resolve, reject) {
  console.log("-----------In startReading FUNCTION---------------------------");
    var failureCallback = function() {
        console.log("Problem while startReading");
        //return "Problem while startReading";
        reject("Problem whjile reading");
    };

    var wrappedSuccess = function(data) {
        console.log( "Data from magnetic card >>>> " + data)
        console.log("startReading success");
        // return data;
        resolve(data);
    };

  cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "startReading", []);
});

module.exports = new MagneticCardHelper();

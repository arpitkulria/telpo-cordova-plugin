
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


// MagneticCardHelper.prototype.startReading = function() {
//   console.log("-----------In startReading FUNCTION---------------------------");
//     var failureCallback = function() {
//         console.log("Problem while startReading");
//         return "Problem while startReading";
//         // reject("Problem whjile reading");
//     };
//
//     var wrappedSuccess = function(data) {
//         console.log( "Data from magnetic card >>>> " + data)
//         console.log("startReading success");
//         return data;
//         // resolve(data);
//     };
//
//   cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "startReading", []);
// };


MagneticCardHelper.prototype.startReading = function() {
  console.log("-----------In startReading FUNCTION---------------------------");
    var failureCallback = function() {
        console.log("Problem while startReading");
        return "Problem while startReading";
        // reject("Problem whjile reading");
    };

    var wrappedSuccess = function(data) {
        console.log( "Data from magnetic card >>>> " + this.data)
        console.log("startReading success");
       return data;
    //resolve(this.data);
    };
    // var promise = new Promise()
  cordova.exec(wrappedSuccess, failureCallback, "MagneticCardHelper", "startReading", []);
};


module.exports = new MagneticCardHelper();

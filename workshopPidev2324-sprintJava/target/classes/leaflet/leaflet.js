// leaflet.js
var L = {
    map: function (id) {
        return {
            setView: function (coords, zoom) {
                console.log('Setting view:', coords, zoom);
            },
            on: function (event, callback) {
                console.log('Event attached:', event);
            }
        };
    },
    tileLayer: function (url, options) {
        return {
            addTo: function (map) {
                console.log('TileLayer added to map');
            }
        };
    }
};

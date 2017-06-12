(function() {
    function redirectUrl() {
        if (window.location.host.indexOf('localhost') >= 0) {
            return 'http://localhost:8000';
        } else {
            return 'https://cfp.bekk.no';
        }
    }

    var token = localStorage.getItem('login_token');
    if (!token) {
        if (window.location.hash.indexOf('access_token=') >= 0) {
            token = window.location.hash
                .substr(1)
                .split('&')
                .map(function(query) {
                    return query.split('=');
                })
                .reduce(function(acc, val) {
                    if (acc) {
                        return acc;
                    } else {
                        if (val[0] === 'id_token') {
                            return val[1];
                        } else {
                            return acc;
                        }
                    }
                }, null);
            localStorage.setItem('login_token', token);
        } else {
            window.location =
                'https://bekk.eu.auth0.com/authorize/?client_id=o49bHOfUDukdCku6Ak4DngHc2wSwv3CT&scope=openid email&response_type=token&redirect_uri=' +
                redirectUrl() +
                '&connection=Bekk';
            return;
        }
    }

    var app = Elm.Main.fullscreen({
        host: window.location.host,
        token: token
    });

    app.ports.reauthenticate.subscribe(function() {
        localStorage.removeItem('login_token');
        window.location =
            'https://bekk.eu.auth0.com/authorize/?client_id=o49bHOfUDukdCku6Ak4DngHc2wSwv3CT&scope=openid email&response_type=token&redirect_uri=' +
            redirectUrl() +
            '&connection=Bekk';
    });
})();

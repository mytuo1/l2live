import { $, $wnd, $body, tween } from "./_utility";

/*------------------------------------------------------------------

  Init Cookie Alert

-------------------------------------------------------------------*/
function initCookieAlert () {
    const self = this;
    let confirmed_cookie_name = 'nk_cookie_alert_dismissed';
    let expiration = 365;
    let showTimeout = 2000;

    // stop if already dismissed
    if (!self.options.enableCookieAlert || document.cookie.indexOf(confirmed_cookie_name) > -1 || window.navigator && window.navigator.CookiesOK) {
        return;
    }

    // create alert
    let $alert = $('<div class="nk-cookie-alert">').hide().append(self.options.templates.cookieAlert).appendTo($body);

    // hide alert
    function hideAlert () {
        tween.to($alert, 0.5, {
            opacity: 0,
            display: 'none'
        });
    }

    // show alert
    function showAlert () {
        tween.set($alert, {
            opacity: 0,
            display: 'none',
            y: 20
        });
        tween.to($alert, 0.5, {
            opacity: 1,
            display: 'block',
            y: 0,
            force3D: true
        });
    }

    // set cookie after confirmation
    function setConfirmed () {
        let exdate = new Date();
            exdate.setDate(exdate.getDate() + expiration);
            exdate = exdate.toUTCString();
        document.cookie = confirmed_cookie_name + '=yes;expires=' + exdate + ';path=/';
    }

    $wnd.on('load', () => {
        setTimeout(showAlert, showTimeout);
    });
    $alert.on('click', '.nk-cookie-alert-confirm', () => {
        hideAlert();
        setConfirmed();
    });
    $alert.on('click', '.nk-cookie-alert-close', hideAlert);
}

export { initCookieAlert };

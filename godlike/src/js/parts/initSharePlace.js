import { $, debounceResize } from "./_utility";

/*------------------------------------------------------------------

  Init Share Place

-------------------------------------------------------------------*/
function initSharePlace () {
    /* nK Share */
    if(typeof $.fn.nkshare !== 'undefined') {
        $('.nk-share-icons').nkshare();
    }

    /* Share Place vertical centering */
    let $share = $('.nk-share-buttons');
    function centerSharePlace () {
        $share.css({
            marginTop: - $share.outerHeight() / 2,
            transform: 'none'
        });
    }

    centerSharePlace();
    debounceResize(centerSharePlace);
}

export { initSharePlace };

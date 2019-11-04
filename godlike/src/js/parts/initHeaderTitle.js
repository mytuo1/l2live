import { $, wndH } from "./_utility";

/*------------------------------------------------------------------

  Init Header Title

-------------------------------------------------------------------*/
function initHeaderTitle () {
    const self = this;
    let $navbarHeader = $('.nk-header');
    let isNavbarOpaque = $navbarHeader.hasClass('nk-header-opaque');
    let isNavbarTransparent = $('.nk-navbar-top').hasClass('nk-header-transparent');
    let $fullHeaderTitle = $('.nk-header-title-full');

    // remove header title padding if navbar opaque
    if(isNavbarOpaque) {
        $('.nk-header-title').css('padding-top', 0);
    }

    self.debounceResize(() => {
        if((isNavbarTransparent || isNavbarOpaque) && (!$fullHeaderTitle.length || !isNavbarOpaque)) {
            return;
        }

        let navH = $navbarHeader.outerHeight() || 0;

        // add header title padding
        if(!isNavbarTransparent && !isNavbarOpaque) {
            $('.nk-header-title').css('padding-top', navH);
        }

        // fix header title height
        if($fullHeaderTitle.length) {
            let headerH = '100vh';

            $fullHeaderTitle.css('height', 'auto');

            if(isNavbarOpaque) {
                headerH = 'calc(100vh - ' + navH + 'px)';
            }

            if($fullHeaderTitle[0].scrollHeight < wndH) {
                $fullHeaderTitle.css('height', headerH);
            }
        }
    });
}

export { initHeaderTitle };

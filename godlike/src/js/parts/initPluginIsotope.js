import { $ } from "./_utility";

/* Isotope */
function initPluginIsotope () {
    if(typeof window.Isotope === 'undefined') {
        return;
    }
    const self = this;

    $('.nk-isotope').each(function () {
        let $grid = $(this).isotope({
            itemSelector: '.nk-isotope-item'
        });
        $grid.imagesLoaded().progress(() => {
            $grid.isotope('layout');
        });
        $grid.on('arrangeComplete', () => {
            self.debounceResize();
        });

        // filter
        let $filter = $(this).prev('.nk-isotope-filter');
        if($filter.length) {
            $filter.on('click', '[data-filter]', function (e) {
                e.preventDefault();
                let filter = $(this).attr('data-filter');

                $(this).addClass('active').siblings().removeClass('active');

                $grid.isotope({
                    filter: filter === '*' ? '' : '[data-filter*=' + filter + ']'
                });
            });
        }
    });
}

export { initPluginIsotope };

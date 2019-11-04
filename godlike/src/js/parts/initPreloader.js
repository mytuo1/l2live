import { $, tween, $wnd, $doc } from "./_utility";

/*------------------------------------------------------------------

  Init Preloader

-------------------------------------------------------------------*/
function initPreloader () {
    const self = this;
    let $preloader = $('.nk-preloader');
    let $preloaderBG = $preloader.find('.nk-preloader-bg');
    let $content = $preloader.find('.nk-preloader-content, .nk-preloader-skip');
    if(!$preloader.length) {
        return;
    }

    let isBusy = 0;
    let isOpened = 1;

    // prepare preloader
    let dataCloseFrames = parseInt($preloaderBG.attr('data-close-frames'), 10);
    let dataCloseSpeed = parseFloat($preloaderBG.attr('data-close-speed'));
    let dataCloseSprites = $preloaderBG.attr('data-close-sprites');
    let dataOpenFrames = parseInt($preloaderBG.attr('data-open-frames'), 10);
    let dataOpenSpeed = parseFloat($preloaderBG.attr('data-open-speed'));
    let dataOpenSprites = $preloaderBG.attr('data-open-sprites');

    function prepareImage (imgSrc, frames) {
        $preloaderBG.css({
            backgroundImage: 'url("' + imgSrc + '")',
            width: frames + '00%'
        });
        tween.set($preloader, {
            opacity: 1,
            display: 'block'
        });
    }

    // close preloader
    self.closePreloader = function (cb) {
        if(!isBusy && isOpened) {
            isBusy = 1;

            if (!dataCloseSprites || !dataCloseSpeed || !dataCloseFrames) {
                tween.to($preloader, 0.3, {
                    opacity: 0,
                    display: 'none',
                    force3D: true,
                    onComplete () {
                        // hide content
                        tween.set($content, {
                            opacity: 0,
                            display: 'none'
                        });

                        if(cb) {
                            cb();
                        }
                        isBusy = 0;
                        isOpened = false;
                    }
                });
                return;
            }

            prepareImage(dataCloseSprites, dataCloseFrames);
            $preloaderBG.css({
                backgroundColor: 'transparent'
            });

            // animate background
            tween.set($preloaderBG, {
                x: '0%'
            });
            tween.to($preloaderBG, dataCloseSpeed, {
                x: '100%',
                ease: SteppedEase.config(dataCloseFrames),
                force3D: true,
                onComplete () {
                    tween.set($preloader, {
                        opacity: 0,
                        display: 'none'
                    });
                    if(cb) {
                        cb();
                    }
                    isBusy = 0;
                    isOpened = false;
                }
            });

            // animate content
            tween.to($content, 0.3, {
                y: '-20px',
                opacity: 0,
                display: 'none',
                force3D: true
            });
        }
    };

    // open preloader
    self.openPreloader = function (cb) {
        if(!isBusy && !isOpened) {
            isBusy = 1;

            if (!dataOpenSprites || !dataOpenSpeed || !dataOpenFrames) {
                if(cb) {
                    cb();
                }
                isBusy = 0;
                return;
            }

            prepareImage(dataOpenSprites, dataOpenFrames);
            $preloaderBG.css({
                backgroundColor: 'transparent'
            });

            // animate background
            tween.set($preloaderBG, {
                x: '100%'
            });
            tween.to($preloaderBG, dataOpenSpeed, {
                x: '0%',
                ease: SteppedEase.config(dataOpenFrames),
                force3D: true,
                onComplete () {
                    if(cb) {
                        cb();
                    }
                    isBusy = 0;
                    isOpened = true;
                }
            });
        }
    };

    // hide preloader after page load
    prepareImage(dataCloseSprites, dataCloseFrames);
    let preloadedImage = 0;
    function firstClosePreloader () {
        self.closePreloader();

        // preload image for opening animation
        if(!preloadedImage && dataOpenSprites) {
            preloadedImage = 1;
            setTimeout(() => {
                let img = new Image;
                img.src = dataOpenSprites;
            }, 1000);
        }
    }
    $wnd.on('load', firstClosePreloader);
    $preloader.on('click', '.nk-preloader-skip', firstClosePreloader);

    // fade between pages
    if(!self.options.enableFadeBetweenPages) {
        return;
    }

    // Internal: Return the `href` component of given URL object with the hash
    // portion removed.
    //
    // location - Location or HTMLAnchorElement
    //
    // Returns String
    function stripHash(href) {
        return href.replace(/#.*/, '')
    }

    $doc.on('click', 'a:not(.no-fade):not([target="_blank"]):not([href^="#"]):not([href^="mailto"]):not([href^="javascript:"])', function (e) {
        let href = this.href;

        // stop if empty href
        if(!href) {
            return;
        }

        // Middle click, cmd click, and ctrl click should open
        // links in a new tab as normal.
        if (e.which > 1 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
            return;
        }

        // Ignore case when a hash is being tacked on the current URL
        if (href.indexOf('#') > -1 && stripHash(href) == stripHash(location.href)) {
            return;
        }

        // Ignore e with default prevented
        if (e.isDefaultPrevented()) {
            return;
        }

        // prevent item clicked
        if($(this).next('.dropdown').length) {
            if($(this).next('.dropdown').css('visibility') === 'hidden' || $(this).parent().hasClass('open')) {
                return;
            }
        }

        e.preventDefault();
        self.openPreloader(function () {
            window.location.href = href;
        });
    });

    // fix safari back button
    // thanks http://stackoverflow.com/questions/8788802/prevent-safari-loading-from-cache-when-back-button-is-clicked
    $wnd.on('pageshow', function(e) {
        if (e.originalEvent.persisted) {
            $preloader.hide();
        }
    });
}

export { initPreloader };

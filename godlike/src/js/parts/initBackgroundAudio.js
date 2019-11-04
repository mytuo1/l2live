import { $, $wnd, isMobile } from "./_utility";

/*------------------------------------------------------------------

  Init Background Audio

-------------------------------------------------------------------*/
function initBackgroundAudio () {
    const self = this;
    let $toggle = $('.nk-bg-audio-toggle');
    if(!self.options.backgroundMusic || typeof soundManager === 'undefined') {
        $toggle.hide();
        return;
    }
    let api;

    // hide / show play icon
    $toggle.find('.nk-bg-audio-play-icon').hide();

    function saveParams () {
        if(api) {
            localStorage.nkBackgroundAudio = JSON.stringify({
                playing: !api.paused && api.playState,
                progress: api.position
            });
        }
    }
    // save on close window and every 20 seconds
    $wnd.on('unload', saveParams);
    setInterval(saveParams, 20000);

    function getParams () {
        let params = {
            playing: self.options.backgroundMusicAutoplay,
            progress: 0
        };

        // restore local data
        if(localStorage && typeof localStorage.nkBackgroundAudio !== 'undefined') {
            let storedData = JSON.parse(localStorage.nkBackgroundAudio);
            params = $.extend(params, storedData);
        }

        // prevent autoplay on mobile devices
        if(isMobile) {
            params.playing = false;
        }

        return params;
    }

    function onPlay () {
        $toggle.find('.nk-bg-audio-play-icon').hide();
        $toggle.find('.nk-bg-audio-pause-icon').show();
    }
    function onStop () {
        $toggle.find('.nk-bg-audio-pause-icon').hide();
        $toggle.find('.nk-bg-audio-play-icon').show();
    }


    let params = getParams();

    // toggle button if autoplay
    if(params.playing) {
        onPlay();
    } else {
        onStop();
    }

    soundManager.onready(() => {
        let firstLoad = 1;
        api = soundManager.createSound({
            onplay () {
                onPlay();
            },
            onresume () {
                onPlay();
            },
            onpause () {
                onStop();
            },
            onstop () {
                onStop();
            },
            volume: self.options.backgroundMusicVolume,
            onload: function (ok) {
                if (!ok && this._iO && this._iO.onerror) {
                    this._iO.onerror();
                }
            },
            onfinish () {
                api.play();
            },
            onbufferchange () {
                // move to saved progress position on first load
                if(firstLoad && api.duration) {
                    firstLoad = 0;
                    api.setPosition(params.progress);
                }
            }
        });

        // autoplay
        if(params.playing) {
            api.play({
                url: self.options.backgroundMusic
            });
        }

        // play / pause
        $toggle.on('click', function () {
            if(api.paused || !api.playState) {
                api.play({
                    url: self.options.backgroundMusic
                });
            } else {
                api.pause();
            }
        });
    });
}

export { initBackgroundAudio };

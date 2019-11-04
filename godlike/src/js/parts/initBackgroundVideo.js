import { $, wndW, wndH, tween, debounceResize } from "./_utility";

/*------------------------------------------------------------------

  Init Background Video

-------------------------------------------------------------------*/
function initBackgroundVideo () {
    let $bg = $('[data-bg-mp4], [data-bg-webm], [data-bg-ogv]');
    if(!$bg.length || typeof window.VideoWorker === 'undefined') {
        return;
    }

    $bg.each(function () {
        let $this = $(this);
        let mp4 = $this.attr('data-bg-mp4') || '';
        let webm = $this.attr('data-bg-webm') || '';
        let ogv = $this.attr('data-bg-ogv') || '';
        let poster = $this.attr('data-bg-poster') || '';
        let url = [];
        if(mp4) {
            url.push('mp4:' + mp4);
        }
        if(webm) {
            url.push('webm:' + webm);
        }
        if(ogv) {
            url.push('ogv:' + ogv);
        }
        url = url.join(',');

        // add background image
        if(poster) {
            $this.css({
                'background-image': 'url("' + poster + '")',
                'background-position': '50% 50%',
                'background-size': 'cover'
            });
        }

        // play video
        if(url) {
            let api = new VideoWorker(url, {
                autoplay: 1,
                loop: 1,
                mute: 1,
                controls: 0
            });

            if(api && api.isValid()) {
                let $video;
                api.getIframe((iframe) => {
                    // add iframe
                    $video = $(iframe);
                    let $parent = $video.parent();
                    tween.set(iframe, {
                        opacity: 0,
                        display: 'none'
                    });
                    $video.appendTo($this);
                    $parent.remove();
                });

                api.on('play', () => {
                    tween.to($video, 0.5, {
                        opacity: 1,
                        display: 'block',
                        force3D: true
                    });
                });

                // cover video on resize
                debounceResize(function () {
                    if(!$video || !$video[0]) {
                        return;
                    }

                    // native video size
                    let vW = $video[0].videoWidth || 1280;
                    let vH = $video[0].videoHeight || 720;

                    if (wndW / vW > wndH / vH) {
                        $video.css({
                            width: wndW,
                            height: 'auto',
                            marginTop: (wndH - vH * wndW / vW) / 2,
                            marginLeft: 0
                        });
                    } else {
                        $video.css({
                            width: 'auto',
                            height: wndH,
                            marginTop: 0,
                            marginLeft: (wndW - vW * wndH / vH) / 2
                        });
                    }
                });
            }
        }
    });
}

export { initBackgroundVideo };

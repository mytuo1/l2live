import { $ } from "./_utility";

/* Typed.js */
function initPluginTypedjs () {
    $('.nk-typed').each(function () {
        let $this = $(this);
        let strings = [];
        $this.children('span').each(function () {
            strings.push($(this).html());
        });
        if(!strings.length) {
            return;
        }
        $this.html('');

        let loop = $this.attr('data-loop') ? $this.attr('data-loop') === 'true' : true;
        let shuffle = $this.attr('data-shuffle') ? $this.attr('data-shuffle') === 'true' : false;
        let typeSpeed = $this.attr('data-type-speed') ? parseInt($this.attr('data-type-speed'), 10) : 30;
        let startDelay = $this.attr('data-start-delay') ? parseInt($this.attr('data-start-delay'), 10) : 0;
        let backSpeed = $this.attr('data-back-speed') ? parseInt($this.attr('data-back-speed'), 10) : 0;
        let backDelay = $this.attr('data-back-delay') ? parseInt($this.attr('data-back-delay'), 10) : 1000;
        let cursor = $this.attr('data-cursor');
            cursor = cursor && cursor !== 'false' ? cursor : cursor === 'false' ? false : '|';

        $('<span>').appendTo($this).typed({
            strings: strings,
            typeSpeed: typeSpeed,
            startDelay: startDelay,
            backSpeed: backSpeed,
            backDelay: backDelay,
            shuffle: shuffle,
            loop: loop,
            loopCount: false,
            showCursor: !!cursor,
            cursorChar: cursor
        });

        // typed js used timeout from startDelay option
        setTimeout(() => {
            $this.addClass('ready');
        }, 0);
    });
}

export { initPluginTypedjs };

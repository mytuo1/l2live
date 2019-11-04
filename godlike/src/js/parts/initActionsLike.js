import { $, tween, $doc, $body } from "./_utility";

/*------------------------------------------------------------------

  Init Actions Like and Heart

-------------------------------------------------------------------*/
function initActionsLike () {
    const self = this;

    // like / dislike animation init
    let $likeAnimation;
    let $dislikeAnimation;
    if(self.options.enableActionLikeAnimation) {
        $likeAnimation = $('<div class="nk-like-animation">' + self.options.templates.likeAnimationLiked + '</div>').appendTo($body);
        $dislikeAnimation = $('<div class="nk-dislike-animation">' + self.options.templates.likeAnimationDisliked + '</div>').appendTo($body);
    }
    function runLikeAnimation (type) {
        let $animateItem = type === 'like' ? $likeAnimation : $dislikeAnimation;
        tween.set($animateItem, {
            scale: 1,
            opacity: 0
        });
        tween.to($animateItem, 0.3, {
            scale: 1.1,
            opacity: 0.5,
            display: 'block',
            ease: Power2.easeIn,
            force3D: true,
            onComplete () {
                tween.to($animateItem, 0.3, {
                    scale: 1.2,
                    opacity: 0,
                    display: 'none',
                    ease: Power2.easeOut,
                    force3D: true
                });
            }
        });
    }

    // heart action
    $doc.on('click', '.nk-action-heart', function (e) {
        e.preventDefault();
        let $like = $(this);

        if($like.hasClass('busy')) {
            return;
        }

        let $num = $like.find('.num');
        let type = $like.hasClass('liked') ? 'dislike' : 'like';
        let $parent = $like.parents('.nk-comment:eq(0), .nk-blog-post:eq(0)').eq(0);
        let updatedNum;
        let updatedIcon;
        $like.addClass('busy');
        self.options.events.actionHeart({
            $dom: $like,
            $parent: $parent,
            type: type,
            currentNum: parseInt($num.text(), 10),
            updateNum (num) {
                $num.text(num);
                updatedNum = 1;
                if(updatedNum && updatedIcon) {
                    $like.removeClass('busy');
                }
            },
            updateIcon () {
                $like[type === 'like' ? 'addClass' : 'removeClass']('liked');
                updatedIcon = 1;
                if(updatedNum && updatedIcon) {
                    $like.removeClass('busy');
                }

                // like / dislike animation
                if(self.options.enableActionLikeAnimation) {
                    runLikeAnimation(type);
                }
            }
        });
    });

    // like action
    $doc.on('click', '.nk-action-like .like-icon, .nk-action-like .dislike-icon', function (e) {
        e.preventDefault();
        let $like = $(this);
        let $parentLike = $like.parent();

        if($parentLike.hasClass('busy')) {
            return;
        }

        let isLiked = $parentLike.hasClass('liked');
        let isDisliked = $parentLike.hasClass('disliked');
        let isDislike = $like.hasClass('dislike-icon');

        let $num = $parentLike.find('.num');
        let $parent = $parentLike.parents('.nk-comment:eq(0), .nk-blog-post:eq(0)').eq(0);
        let type = isDislike ? 'dislike' : 'like';
        let updatedNum;
        let updatedIcon;
        $parentLike.addClass('busy');
        self.options.events.actionLike({
            $dom: $like,
            $parent: $parent,
            type: type,
            isLiked: isLiked,
            isDisliked: isDisliked,
            currentNum: parseInt($num.text(), 10),
            updateNum (num) {
                $num.text((num > 0 ? '+' : '') + num);
                updatedNum = 1;
                if(updatedNum && updatedIcon) {
                    $parentLike.removeClass('busy');
                }
            },
            updateIcon () {
                $parentLike.removeClass('liked disliked');
                if(!(isLiked && !isDislike || isDisliked && isDislike)) {
                    $parentLike.addClass(type === 'like' ? 'liked' : 'disliked');
                }
                updatedIcon = 1;
                if(updatedNum && updatedIcon) {
                    $parentLike.removeClass('busy');
                }

                // like / dislike animation
                if(self.options.enableActionLikeAnimation) {
                    if(type === 'like' && !isLiked || type === 'dislike' && !isDisliked) {
                        runLikeAnimation(type);
                    }
                }
            }
        });
    });
}

export { initActionsLike };

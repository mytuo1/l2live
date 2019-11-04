import { $ } from "./_utility";

/*------------------------------------------------------------------

  Init Blog

-------------------------------------------------------------------*/
function initBlog () {
    let $blogList = $('.nk-blog-list');

    // add hover classname
    $blogList.on('mouseover', '.nk-blog-post .nk-post-thumb, .nk-blog-post .nk-post-content', function () {
        $(this).parents('.nk-blog-post:eq(0)').addClass('hover');
    }).on('mouseleave', '.nk-blog-post .nk-post-thumb, .nk-blog-post .nk-post-content', function () {
        $(this).parents('.nk-blog-post:eq(0)').removeClass('hover');
    });
}

export { initBlog };

import { $ } from "./_utility";

/*------------------------------------------------------------------

  Init Blog

-------------------------------------------------------------------*/
function initTeamMembers () {
    let $blogList = $('.nk-team-member');

    // add hover classname
    $blogList.on('mouseover', '.nk-team-member-photo, .nk-team-member-info', function () {
        $(this).parents('.nk-team-member:eq(0)').addClass('hover');
    }).on('mouseleave', '.nk-team-member-photo, .nk-team-member-info', function () {
        $(this).parents('.nk-team-member:eq(0)').removeClass('hover');
    });
}

export { initTeamMembers };

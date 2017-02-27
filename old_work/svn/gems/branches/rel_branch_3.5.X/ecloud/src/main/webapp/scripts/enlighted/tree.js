//common function to show floor plan for selected node
$.fn.processNode = function() {

    $("#treeviewdiv span").removeClass("nodeselected");
    // apply css on selected node
    $(this).addClass("nodeselected");
}

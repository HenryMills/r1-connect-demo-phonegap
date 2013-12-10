document.addEventListener("deviceready", onDeviceReady, false);

function onDeviceReady() {
    R1SDK.isStarted(function(started) {
        if (started) {
            $("#main_error").hide();
            $("#main_normal").show();

            R1Emitter.isStarted(function(emitter_started) {
                if (emitter_started) {
                    $("#emitter_options").show();
                    configureEmitterOptions();
                } else
                    $("#emitter_options").hide();
            });
            R1Push.isStarted(function(push_started) {
                if (push_started) {
                    $("#push_options").show();
                    configurePushOptions();
                } else
                    $("#push_options").hide();
            });

            configureSharedOptions();
        } else {
            $("#main_error").show();
            $("#main_normal").hide();
        }
    });
}

function configureSharedOptions() {
    R1SDK.getApplicationUserId(function(applicationUserId) {
        var appUserIdInput = $('#shared_options_page_app_user_id');

        if (applicationUserId != null)
            appUserIdInput.val(applicationUserId);
        appUserIdInput.on('input', function(e) {
            R1SDK.setApplicationUserId(appUserIdInput.val());
        });
    });

    R1LocationService.isEnabled(function(locationEnabled) {
        var isLocationEnabledField = $('#shared_options_location_page_enabled');

        isLocationEnabledField.val(locationEnabled ? 'on' : 'off').change();

        if (locationEnabled)
            $('#shared_options_location_page_info').show();
        else
            $('#shared_options_location_page_info').hide();

        isLocationEnabledField.change(function() {

            var enabled = (isLocationEnabledField.val() == "on");

            R1LocationService.setEnabled(enabled);

            if (enabled)
                $('#shared_options_location_page_info').show();
            else
                $('#shared_options_location_page_info').hide();
        });
    });

    R1LocationService.getAutoupdateTimeout(function(autoupdateTimeout) {
        $('#shared_options_location_page_autoupdate_timeout').val(autoupdateTimeout);
    });

    R1LocationService.getState(function(state) {
        $("#shared_options_location_page_info_state").html(state);

        if (state == "Wait Next Update")
            $("#shared_options_location_page_update_now").show();
        else
            $("#shared_options_location_page_update_now").hide();

        document.addEventListener("R1LocationService.state", function(event) {
            $("#shared_options_location_page_info_state").html(event.state);

            if (event.state == "Wait Next Update")
                $("#shared_options_location_page_update_now").show();
            else
                $("#shared_options_location_page_update_now").hide();

        }, false);
    });

    R1LocationService.getCoordinate(function(coordinate) {
        if (coordinate == null)
            $("#shared_options_location_page_info_location").html("Unknown");
        else
            $("#shared_options_location_page_info_location").html("Lat: " + coordinate.latitude + "<br/>Lon: " + coordinate.longitude);

        document.addEventListener("R1LocationService.coordinate", function(event) {
            if (event == null)
                $("#shared_options_location_page_info_location").html("Unknown");
            else
                $("#shared_options_location_page_info_location").html("Lat: " + event.latitude + "<br/>Lon: " + event.longitude);
        }, false);
    });

}

function configureEmitterOptions() {
    R1Emitter.getAppName(function(appName) {
        var appNameInput = $('#emitter_options_page_app_name');

        if (appName != null)
            appNameInput.val(appName);
        appNameInput.on('input', function(e) {
            R1Emitter.setAppName(appNameInput.val());
        });
    });

    R1Emitter.getAppId(function(appId) {
        var appIdInput = $('#emitter_options_page_app_id');

        if (appId != null)
            appIdInput.val(appId);
        appIdInput.on('input', function(e) {
            R1Emitter.setAppId(appIdInput.val());
        });
    });


    R1Emitter.getAppVersion(function(appVersion) {
        var appVersionInput = $('#emitter_options_page_app_version');

        if (appVersion != null)
            appVersionInput.val(appVersion);
        appVersionInput.on('input', function(e) {
            R1Emitter.setAppVersion(appVersionInput.val());
        });
    });

    R1Emitter.getSessionTimeout(function(sessionTimeout) {
        var sessionTimeoutInput = $('#emitter_options_page_session_timeout');

        sessionTimeoutInput.val(sessionTimeout);
        sessionTimeoutInput.on('input', function(e) {
            R1Emitter.setSessionTimeout(sessionTimeoutInput.val());
        });
    });


}

function configurePushOptions() {
    R1Push.isEnabled(function(isPushEnabled) {
        var isPushEnabledField = $('#push_options_page_push_enabled');

        isPushEnabledField.val(isPushEnabled ? 'on' : 'off').change()

        isPushEnabledField.change(function() {
            R1Push.setEnabled((isPushEnabledField.val() == "on"));
        });
    });

    pushUpdateTagsList();

    if (!R1SDK.isIOS()) {
        $("#push_options_page_badge_number_group").hide();

        return;
    }

    R1Push.getDeviceToken(function(deviceToken) {
        if (deviceToken == null)
            $("#push_options_page_device_token").html("Unknown");
        else
            $("#push_options_page_device_token").html(deviceToken);

        document.addEventListener("R1Push.deviceToken", function(event) {
            if (event == null || event.deviceToken == null)
                $("#push_options_page_device_token").html("Unknown");
            else
                $("#push_options_page_device_token").html(event.deviceToken);
        }, false);
    });

    R1Push.getBadgeNumber(function(badgeNumber) {
        $('#push_options_page_badge_number').val(badgeNumber);
    });

    document.addEventListener("R1Push.foregroundNotification", function(event) {
                              pushNotificationReceived("Foreground Notification", event);
    }, false);
    document.addEventListener("R1Push.backgroundNotification", function(event) {
                              pushNotificationReceived("Background Notification", event);
    }, false);
}

function pushNotificationReceived(type, notification)
{
    try
    {
        alert(type+"\n"+event.aps.alert);
    }
    catch (err)
    {
        alert(JSON.stringify(err));
    }
}

function pushUpdateTagsList() {
    R1Push.getTags(function(tags) {
        var tagsList = $('#push_options_page_tags_list');

        tagsList.empty();

        tagsList.append('<li data-role="list-divider">Tags:</li>');

        for (var i in tags) {
            if (!tags.hasOwnProperty(i))
                continue;

            var tag = tags[i];

            tagsList.append('<li data-icon="delete"><a href="javascript:pushDeleteTag(\'' + tag + '\')">' + tag + '</a></li>');

        }

        if (tagsList.hasClass('ui-listview')) {
            tagsList.listview("refresh");
        } else {
            tagsList.trigger('create');
        }
    });

}

function pushDeleteTag(tag) {
    R1Push.removeTag(tag);
    pushUpdateTagsList();
}

function pushAddTagPressed() {
    var newTag = $('#push_options_page_new_tag').val();
    $('#push_options_page_new_tag').val("");

    $("#popupAddTag").popup("close");

    R1Push.addTag(newTag);
    pushUpdateTagsList();
}

function pushChangeBadgePressed() {
    R1Push.setBadgeNumber(parseInt($('#push_options_page_badge_number').val()));
}

function locationChangeAutoupdateTimeoutPressed() {
    R1LocationService.setAutoupdateTimeout(parseInt($('#shared_options_location_page_autoupdate_timeout').val()));
}

function locationUpdateNowPressed() {
    R1LocationService.updateNow();
}

function objectFromField(field) {
    var parametersStr = field.val();
    var parameters = null
    if (parametersStr != "") {
        try {
            parameters = JSON.parse(parametersStr);
        } catch (err) {
            alert("Not valid parameters JSON");
            return;
        }
    }

    return parameters;
}

function emitEventPressed() {
    var name = $("#emit_event_page_name").val();

    if (name == "") {
        alert("Event name is empty");
        return;
    }

    var parameters = objectFromField($("#emit_event_page_parameters"));

    R1Emitter.emitEvent(name, parameters);
}

function emitActionPressed() {
    var action = $("#emit_action_page_action").val();

    if (action == "") {
        alert("Event action is empty");
        return;
    }

    var label = $("#emit_action_page_label").val();
    var value = $("#emit_action_page_value").val();

    var otherInfo = objectFromField($("#emit_action_page_other_info"));

    R1Emitter.emitAction(action, label, value, otherInfo);
}

function emitLoginPressed() {
    var userID = $("#emit_login_page_user_id").val();
    var userName = $("#emit_login_page_user_name").val();

    var otherInfo = objectFromField($("#emit_login_page_other_info"));

    R1Emitter.emitLogin(userID, userName, otherInfo);
}

function emitRegistrationPressed() {
    var userID = $("#emit_registration_page_user_id").val();
    var userName = $("#emit_registration_page_user_name").val();
    var email = $("#emit_registration_page_email").val();
    var streetAddress = $("#emit_registration_page_street_address").val();
    var phone = $("#emit_registration_page_phone").val();
    var city = $("#emit_registration_page_city").val();
    var state = $("#emit_registration_page_state").val();
    var zip = $("#emit_registration_page_zip").val();

    var otherInfo = objectFromField($("#emit_registration_page_other_info"));

    R1Emitter.emitRegistration(userID, userName, email, streetAddress, phone, city, state, zip, otherInfo);
}

function fbConnectAddPermissionPressed() {
    var name = $('#emit_fbconnect_page_new_permission_name').val();
    $('#emit_fbconnect_page_new_permission_name').val("");

    var granted = ($('#emit_fbconnect_page_new_permission_granted').val() == "yes");
    $('#emit_fbconnect_page_new_permission_granted').val("yes");

    $("#emit_fbconnect_page_popupAddPermission").popup("close");

    if (name == "")
        return;

    addSocialPermission({
        "name": name,
        "granted": granted
    }, $("#emit_fbconnect_page_social_permissions_list"), deleteFBSocialPermission);
}

function addSocialPermission(socialPermission, listview, deleteMethod) {
    var lastIndex = listview.attr('data-last-index');
    if (lastIndex == null)
        lastIndex = 1;
    else
        lastIndex = parseInt(lastIndex) + 1;

    listview.attr('data-last-index', lastIndex);

    listview.append('<li data-icon="delete" data-social-permission=\'' + JSON.stringify(socialPermission) + '\' data-social-permission-index="' + lastIndex + '"><a href="javascript:' + deleteMethod + '(\'' + lastIndex + '\')">' + socialPermission.name + ': ' + (socialPermission.granted ? "Granted" : "Not Provided") + '</a></li>');

    if (listview.hasClass('ui-listview'))
        listview.listview("refresh");
    else
        listview.trigger('create');
}

function deleteFBSocialPermission(index) {
    var listview = $("#emit_fbconnect_page_social_permissions_list");

    $("#emit_fbconnect_page_social_permissions_list li").each(function() {
        if ($(this).attr('data-social-permission-index') == index)
            $(this).remove();
    });

    listview.listview("refresh");
}

function emitFBConnectPressed() {
    var userID = $("#emit_fbconnect_page_user_id").val();
    var userName = $("#emit_fbconnect_page_user_name").val();

    var permissions = [];

    $("#emit_fbconnect_page_social_permissions_list li").each(function() {
        if ($(this).attr('data-social-permission') != null) {
            var obj = JSON.parse($(this).attr('data-social-permission'));
            permissions.push(obj);
        }
    });


    var otherInfo = objectFromField($("#emit_fbconnect_page_other_info"));

    R1Emitter.emitFBConnect(userID, userName, permissions, otherInfo);

}

function tConnectAddPermissionPressed() {
    var name = $('#emit_tconnect_page_new_permission_name').val();
    $('#emit_tconnect_page_new_permission_name').val("");

    var granted = ($('#emit_tconnect_page_new_permission_granted').val() == "yes");
    $('#emit_tconnect_page_new_permission_granted').val("yes");

    $("#emit_tconnect_page_popupAddPermission").popup("close");

    if (name == "")
        return;

    addSocialPermission({
        "name": name,
        "granted": granted
    }, $("#emit_tconnect_page_social_permissions_list"), deleteTSocialPermission);
}

function deleteTSocialPermission(index) {
    var listview = $("#emit_tconnect_page_social_permissions_list");

    $("#emit_tconnect_page_social_permissions_list li").each(function() {
        if ($(this).attr('data-social-permission-index') == index)
            $(this).remove();
    });

    listview.listview("refresh");
}

function emitTConnectPressed() {
    var userID = $("#emit_tconnect_page_user_id").val();
    var userName = $("#emit_tconnect_page_user_name").val();

    var permissions = [];

    $("#emit_tconnect_page_social_permissions_list li").each(function() {
        if ($(this).attr('data-social-permission') != null) {
            var obj = JSON.parse($(this).attr('data-social-permission'));
            permissions.push(obj);
        }
    });


    var otherInfo = objectFromField($("#emit_tconnect_page_other_info"));

    R1Emitter.emitTConnect(userID, userName, permissions, otherInfo);

}

function emitTransactionPressed() {
    var transactionID = $("#emit_transaction_page_transaction_id").val();
    var storeID = $("#emit_transaction_page_store_id").val();
    var storeName = $("#emit_transaction_page_store_name").val();
    var cartID = $("#emit_transaction_page_cart_id").val();
    var orderID = $("#emit_transaction_page_order_id").val();
    var totalSale = parseFloat($("#emit_transaction_page_total_sale").val());
    var currency = $("#emit_transaction_page_currency").val();
    var shippingCosts = parseFloat($("#emit_transaction_page_shipping_costs").val());
    var transactionTax = parseFloat($("#emit_transaction_page_transaction_tax").val());

    var otherInfo = objectFromField($("#emit_transaction_page_other_info"));

    R1Emitter.emitTransaction(transactionID, storeID, storeName, cartID, orderID, totalSale, currency, shippingCosts, transactionTax, otherInfo);

}

function emitTransactionItemPressed() {
    var transactionID = $("#emit_transaction_item_page_transaction_id").val();
    var lineItem = LineItem("emit_transaction_item_page");

    var otherInfo = objectFromField($("#emit_transaction_item_page_other_info"));

    R1Emitter.emitTransactionItem(transactionID, lineItem, otherInfo);
}

function emitCartCreatePressed() {
    var cartID = $("#emit_cart_create_page_cart_id").val();

    var otherInfo = objectFromField($("#emit_cart_create_page_other_info"));

    R1Emitter.emitCartCreate(cartID, otherInfo);
}

function emitCartDeletePressed() {
    var cartID = $("#emit_cart_delete_page_cart_id").val();

    var otherInfo = objectFromField($("#emit_cart_delete_page_other_info"));

    R1Emitter.emitCartDelete(cartID, otherInfo);
}

function emitAddToCartPressed() {
    var cartID = $("#emit_add_to_cart_page_cart_id").val();
    var lineItem = LineItem("emit_add_to_cart_page");

    var otherInfo = objectFromField($("#emit_add_to_cart_page_other_info"));

    R1Emitter.emitAddToCart(cartID, lineItem, otherInfo);
}

function emitDeleteFromCartPressed() {
    var cartID = $("#emit_delete_from_cart_page_cart_id").val();
    var lineItem = LineItem("emit_delete_from_cart_page");

    var otherInfo = objectFromField($("#emit_delete_from_cart_page_other_info"));

    R1Emitter.emitDeleteFromCart(cartID, lineItem, otherInfo);
}

function emitUpgradePressed() {
    var otherInfo = objectFromField($("#emit_upgrade_page_other_info"));

    R1Emitter.emitUpgrade(otherInfo);
}

function emitTrialUpgradePressed() {
    var otherInfo = objectFromField($("#emit_trial_upgrade_page_other_info"));

    R1Emitter.emitTrialUpgrade(otherInfo);
}

function emitScreenViewPressed() {
    var documentTitle = $("#emit_screen_view_page_document_title").val();
    var contentDescription = $("#emit_screen_view_page_content_description").val();
    var documentLocationUrl = $("#emit_screen_view_page_document_location_url").val();
    var documentHostName = $("#emit_screen_view_page_document_host_name").val();
    var documentPath = $("#emit_screen_view_page_document_path").val();

    var otherInfo = objectFromField($("#emit_screen_view_page_other_info"));

    R1Emitter.emitScreenView(documentTitle, contentDescription, documentLocationUrl, documentHostName, documentPath, otherInfo);
}

function LineItem(idPrefix) {
    var lineItem = {};

    var productIdVal = $("#" + idPrefix + "_line_item_product_id").val();
    if (productIdVal != null)
        lineItem.productID = productIdVal;

    var productNameVal = $("#" + idPrefix + "_line_item_product_name").val();
    if (productNameVal != null)
        lineItem.productName = productNameVal;

    var quantityVal = $("#" + idPrefix + "_line_item_quantity").val();
    if (quantityVal != null)
        lineItem.quantity = parseInt(quantityVal);

    var unitOfMeasureVal = $("#" + idPrefix + "_line_item_unit_of_measure").val();
    if (unitOfMeasureVal != null)
        lineItem.unitOfMeasure = unitOfMeasureVal;

    var msrPriceVal = $("#" + idPrefix + "_line_item_msr_price").val();
    if (msrPriceVal != null)
        lineItem.msrPrice = parseFloat(msrPriceVal);

    var pricePaidVal = $("#" + idPrefix + "_line_item_price_paid").val();
    if (pricePaidVal != null)
        lineItem.pricePaid = parseFloat(pricePaidVal);

    var currencyVal = $("#" + idPrefix + "_line_item_currency").val();
    if (currencyVal != null)
        lineItem.currency = currencyVal;

    var itemCategoryVal = $("#" + idPrefix + "_line_item_category").val();
    if (itemCategoryVal != null)
        lineItem.itemCategory = itemCategoryVal;

    return lineItem;
}
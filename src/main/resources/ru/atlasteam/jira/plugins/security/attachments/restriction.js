require(['jquery', 'jira/util/events/reasons', 'jira/util/events/types'], function($, JiraEventReasons, JiraEvents) {
    AJS.toInit(function () {
        var $select = $('#commentLevel');

        function init(context) {
            var issueKey = $('#key-val').data('issue-key');
            if (issueKey)
                $.ajax({
                    type: 'GET',
                    url: AJS.contextPath() + '/rest/security/1.0/attachmentrestriction/issue/' + issueKey,
                    success: function (result) {
                        var $attachmentPanel = $(context).find('#attachmentmodule').andSelf().filter('#attachmentmodule');
                        var $manageAttachments = $('#manage-attachments');

                        for (var i = 0; i < result.length; i++) {
                            var $attachmentPanelItem = $attachmentPanel.find('a[href^="' + AJS.contextPath() + '/secure/attachment/' + result[i].attachmentId + '"]').parents('li.attachment-content');
                            var $menageAttachmentItem = $manageAttachments.find('a[href^="' + AJS.contextPath() + '/secure/attachment/' + result[i].attachmentId + '"]').parents('tr');

                            if (!result[i].allowedToDownload) {
                                $attachmentPanelItem.remove();
                                $menageAttachmentItem.remove();
                                $attachmentPanel.find('#aszip').parent().remove();
                                $manageAttachments.find('#aszipbutton').remove();
                            } else if ($attachmentPanelItem.find('.attachment-restriction').length == 0) {
                                var thumbnailAttachment = $attachmentPanelItem.parent().attr('id') == 'attachment_thumbnails';
                                var html = buildHtml(result[i].attachmentId, result[i].restrictionName, result[i].allowedToRestrict, thumbnailAttachment ? 'div' : 'dd', true, true);
                                if (thumbnailAttachment)
                                    $attachmentPanelItem.find('div.attachment-thumb').before(html);
                                else {
                                    if ($attachmentPanelItem.find('dd.attachment-delete').length > 1) {
                                        $attachmentPanelItem.find('dd.attachment-delete:last').after(html);
                                        html = buildHtml(result[i].attachmentId, result[i].restrictionName, result[i].allowedToRestrict, thumbnailAttachment ? 'div' : 'dd', true, false);
                                        $attachmentPanelItem.find('dd.attachment-delete:first').after(html);
                                    } else {
                                        $attachmentPanelItem.find('dd.attachment-delete').after(html);
                                    }
                                }
                            }
                        }

                        $attachmentPanel.show();

                        $manageAttachments.show().find('#issue-attachments-table tbody tr').each(function(index) {
                            $(this).find('td:first-child').text(index + 1);
                        });
                    },
                    error: function (request) {
                        alert(request.responseText);
                    }
                });
        }

        function initAttachFileDialog(context) {
            var $form = $(context).find('form#attach-file');
            $form.attr('action', 'UtilsAttachRestrictedFile.jspa');

            var html = '';
            html += '<div>';
            html +=     '<input name="attachmentRestriction" type="hidden" value="">';
            html +=     buildHtml('all', null, true, 'div', false, true);
            html += '</div>';

            $form.find('div.file-input-list').after(html);
        }

        function buildHtml(attachmentId, restrictionName, allowedToRestrict, elementType, showTitle, addDropDown) {
            var html = '';
            if (allowedToRestrict) {
                html += '<' + elementType + ' class="attachment-restriction aui-dropdown2-trigger" aria-haspopup="true" aria-owns="attachment-restriction-' + attachmentId + '">';

                if (restrictionName != null) {
                    html += '<a href="#" title="' + AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.locked', restrictionName) + '">';
                    html +=     '<span class="icon icon-locked"></span>';
                } else {
                    html += showTitle ? '<a href="#" class="attachment-restriction-unlock-hover" title="' + AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.unlocked') + '">' : '<a href="#">';
                    html +=     '<span class="icon icon-unlocked"></span>';
                }
                html +=         '<span class="icon drop-menu"></span>';
                html +=     '</a>';
                if (!showTitle)
                    html +=         '<span class="attachment-restriction-current">' + AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.all.unlocked') + '</span>';

                if (addDropDown) {
                    $select = $select.length ? $select : $('#commentLevel');
                    var dropdownClass = showTitle ? 'attachment-restriction-dropdown' : '';
                    html +=     '<div id="attachment-restriction-' + attachmentId + '" class="aui-style-default aui-dropdown2 ' + dropdownClass + '">';
                    html +=         '<div class="aui-dropdown2-section">';
                    html +=             '<ul>';
                    html +=                 '<li><a href="#" data-id="">' + $select.find('option:first').text() + '</a></li>';
                    html +=             '</ul>';
                    html +=         '</div>';
                    $select.find('optgroup').each(function() {
                        html +=     '<div class="aui-dropdown2-section">';
                        html +=         '<div class="aui-dropdown2-heading">';
                        html +=             '<strong>' + $(this).attr('label') + '</strong>';
                        html +=         '</div>';
                        html +=         '<ul>';
                        $(this).find('option').each(function() {
                            html +=         '<li><a href="#" data-id="' + $(this).val() + '">' + $(this).text() + '</a></li>';
                        });
                        html +=         '</ul>';
                        html +=     '</div>';
                    });
                    html +=     '</div>';
                }

                html += '</' + elementType + '>';
            } else {
                html += '<' + elementType + ' class="attachment-restriction attachment-restriction-no-drop-menu">';

                if (restrictionName != null) {
                    html +=     '<a title="' + AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.locked', restrictionName) + '">';
                    html +=         '<span class="icon icon-locked"></span>';
                    html +=     '</a>';
                } else
                    html +=     '<a><span class="icon"></span></a>';

                html += '</' + elementType + '>';
            }
            return html;
        }

        $(document).on('aui-dropdown2-show', '.attachment-restriction-dropdown', function() {
            $('[aria-controls="' + $(this).attr('id') + '"]').parents('li.attachment-content').addClass('attachment-restriction-hover');
        });

        $(document).on('aui-dropdown2-hide', '.attachment-restriction-dropdown', function() {
            $('[aria-controls="' + $(this).attr('id') + '"]').parents('li.attachment-content').removeClass('attachment-restriction-hover');
        });

        $(document).on('click', '.attachment-restriction-dropdown a', function(e) {
            e.preventDefault();
            var $a = $(this);
            var restriction = $a.data('id');
            var restrictionName = $a.text();
            var attachmentId = $a.parents('.attachment-restriction-dropdown').attr('id').substring('attachment-restriction-'.length);

            if (restriction) {
                $.ajax({
                    type: 'POST',
                    url: AJS.contextPath() + '/rest/security/1.0/attachmentrestriction/' + attachmentId,
                    data: {
                        atl_token: atl_token(),
                        restriction: restriction
                    },
                    success: function() {
                        var $iconContainer = $('[aria-controls="attachment-restriction-' + attachmentId + '"] > a');
                        $iconContainer.find('.icon').removeClass('icon-unlocked').addClass('icon-locked');
                        $iconContainer.attr('title', AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.locked', restrictionName)).removeClass('attachment-restriction-unlock-hover');
                    },
                    error: function (request) {
                        alert(request.responseText);
                    }
                });
            } else {
                $.ajax({
                    type: 'DELETE',
                    url: AJS.contextPath() + '/rest/security/1.0/attachmentrestriction/' + attachmentId,
                    data: {
                        atl_token: atl_token()
                    },
                    success: function() {
                        var $iconContainer = $('[aria-controls="attachment-restriction-' + attachmentId + '"] > a');
                        $iconContainer.find('.icon').removeClass('icon-locked').addClass('icon-unlocked');
                        $iconContainer.attr('title', AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.unlocked')).addClass('attachment-restriction-unlock-hover');
                    },
                    error: function (request) {
                        alert(request.responseText);
                    }
                });
            }
        });

        $(document).on('click', 'div#attachment-restriction-all a', function(e) {
            e.preventDefault();
            var $a = $(this);
            var restriction = $a.data('id');
            var restrictionName = $a.text();
            var $iconContainer = $('[aria-controls="attachment-restriction-all"] > a');
            var $currentLevel = $('[aria-controls="attachment-restriction-all"] .attachment-restriction-current');

            $('input[name="attachmentRestriction"]').val(restriction);
            if (restriction) {
                $iconContainer.find('.icon').removeClass('icon-unlocked').addClass('icon-locked');
                $currentLevel.text(AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.all.locked', restrictionName));
            } else {
                $iconContainer.find('.icon').removeClass('icon-locked').addClass('icon-unlocked');
                $currentLevel.text(AJS.I18n.getText('ru.atlasteam.jira.plugins.security.attachmentrestriction.all.unlocked'));
            }
        });

        JIRA.bind(JiraEvents.NEW_CONTENT_ADDED, function (e, context, reason) {
            if ((reason == JiraEventReasons.panelRefreshed && (context.is('#attachmentmodule') || $(context).is('#file_attachments') || $(context).is("#attachment_thumbnails"))) || (reason == JiraEventReasons.dialogReady && $(context).has('#issue-attachments-table').length))
                init($('#attachmentmodule'));
            if (reason == JiraEventReasons.dialogReady && $(context).has('form#attach-file').length)
                initAttachFileDialog(context);
        });
    });
});

package ru.atlasteam.jira.plugins.security.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SecureSelectCFType extends SelectCFType {
    private final SecuritySchemeService securitySchemeService;

    public SecureSelectCFType(CustomFieldValuePersister customFieldValuePersister, OptionsManager optionsManager, GenericConfigManager genericConfigManager, JiraBaseUrls jiraBaseUrls, SecuritySchemeService securitySchemeService) {
        super(customFieldValuePersister, optionsManager, genericConfigManager, jiraBaseUrls);
        this.securitySchemeService = securitySchemeService;
    }

    @Override
    public Option getValueFromIssue(CustomField field, Issue issue) {
        if (securitySchemeService.isAccessibleForLoggedInUser(field, issue))
            return super.getValueFromIssue(field, issue);
        return null;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> result = super.getVelocityParameters(issue, field, fieldLayoutItem);
        if (securitySchemeService.isAccessibleForLoggedInUser(field, issue))
            result.put("viewTemplate", "templates/plugins/fields/edit/edit-select.vm");
        return result;
    }

    @Override
    public String getChangelogValue(CustomField field, Option value) {
        return null;
    }

    @Nonnull
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SecuritySchemeConfigItem());
        return configurationItemTypes;
    }
}

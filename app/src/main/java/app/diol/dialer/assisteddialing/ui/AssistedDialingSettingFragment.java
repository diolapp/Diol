/*
 *  Copyright (C) 2019  The Diol App Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.diol.dialer.assisteddialing.ui;

import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.telephony.TelephonyManager;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.diol.R;
import app.diol.dialer.assisteddialing.AssistedDialingMediator;
import app.diol.dialer.assisteddialing.ConcreteCreator;
import app.diol.dialer.assisteddialing.CountryCodeProvider;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * The setting for Assisted Dialing
 */
public class AssistedDialingSettingFragment extends PreferenceFragment {

    private CountryCodeProvider countryCodeProvider;
    private AssistedDialingMediator assistedDialingMediator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assistedDialingMediator =
                ConcreteCreator.createNewAssistedDialingMediator(
                        getContext().getSystemService(TelephonyManager.class), getContext());

        countryCodeProvider =
                ConcreteCreator.getCountryCodeProvider(
                        ConfigProviderComponent.get(getContext()).getConfigProvider());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.assisted_dialing_setting);
        SwitchPreference switchPref =
                (SwitchPreference)
                        findPreference(getContext().getString(R.string.assisted_dialing_setting_toggle_key));

        ListPreference countryChooserPref =
                (ListPreference)
                        findPreference(getContext().getString(R.string.assisted_dialing_setting_cc_key));

        updateCountryChoices(countryChooserPref);
        updateCountryChooserSummary(countryChooserPref);

        countryChooserPref.setOnPreferenceChangeListener(this::updateListSummary);
        switchPref.setOnPreferenceChangeListener(this::logIfUserDisabledFeature);
    }

    private void updateCountryChooserSummary(ListPreference countryChooserPref) {
        String defaultSummaryText = countryChooserPref.getEntries()[0].toString();

        if (countryChooserPref.getEntry().equals(defaultSummaryText)) {
            Optional<String> userHomeCountryCode = assistedDialingMediator.userHomeCountryCode();
            if (userHomeCountryCode.isPresent()) {
                CharSequence[] entries = countryChooserPref.getEntries();
                try {
                    CharSequence regionalDisplayName =
                            entries[countryChooserPref.findIndexOfValue(userHomeCountryCode.get())];
                    countryChooserPref.setSummary(
                            getContext()
                                    .getString(
                                            R.string.assisted_dialing_setting_cc_default_summary, regionalDisplayName));
                } catch (ArrayIndexOutOfBoundsException e) {
                    // This might happen if there is a mismatch between the automatically
                    // detected home country, and the countries currently eligible to select in the settings.
                    LogUtil.i(
                            "AssistedDialingSettingFragment.onCreate",
                            "Failed to find human readable mapping for country code, using default.");
                }
            }
        } else {
            countryChooserPref.setSummary(countryChooserPref.getEntry());
        }
    }

    /**
     * Filters the default entries in the country chooser by only showing those countries in which the
     * feature in enabled.
     */
    private void updateCountryChoices(ListPreference countryChooserPref) {

        List<DisplayNameAndCountryCodeTuple> defaultCountryChoices =
                buildDefaultCountryChooserKeysAndValues(countryChooserPref);

        // Always include the default preference.
        List<CharSequence> newKeys = new ArrayList<>();
        List<CharSequence> newValues = new ArrayList<>();
        newKeys.add(countryChooserPref.getEntries()[0]);
        newValues.add(countryChooserPref.getEntryValues()[0]);

        for (DisplayNameAndCountryCodeTuple tuple : defaultCountryChoices) {
            if (countryCodeProvider.isSupportedCountryCode(tuple.countryCode().toString())) {
                newKeys.add(tuple.countryDisplayname());
                newValues.add(tuple.countryCode());
            }
        }

        countryChooserPref.setEntries(newKeys.toArray(new CharSequence[newKeys.size()]));
        countryChooserPref.setEntryValues(newValues.toArray(new CharSequence[newValues.size()]));

        if (!newValues.contains(countryChooserPref.getValue())) {
            ameliorateInvalidSelectedValue(countryChooserPref);
        }
    }

    /**
     * Restore an invalid user selected value to the default value.
     *
     * <p>In the Assisted Dialing settings in Dialer, this state is possible when a user selected a
     * country code, and then that country code was removed from our filtered list, typically via a
     * change in the available countries provided by a server side flag.
     *
     * @param countryChooserPref The list preference to restore to default when an invalid value is
     *                           detected.
     */
    private void ameliorateInvalidSelectedValue(ListPreference countryChooserPref) {
        // Reset the preference value to the default value.
        countryChooserPref.setValue(countryChooserPref.getEntryValues()[0].toString());
        LogUtil.i(
                "AssistedDialingSettingFragment.ameliorateInvalidSelectedValue",
                "Reset the country chooser preference to the default value.");
    }

    private List<DisplayNameAndCountryCodeTuple> buildDefaultCountryChooserKeysAndValues(
            ListPreference countryChooserPref) {
        CharSequence[] keys = countryChooserPref.getEntries();
        CharSequence[] values = countryChooserPref.getEntryValues();

        if (keys.length != values.length) {
            throw new IllegalStateException("Unexpected mismatch in country chooser key/value size");
        }

        List<DisplayNameAndCountryCodeTuple> displayNamesandCountryCodes = new ArrayList<>();
        // getCountry() is actually getRegion() and conforms to the iso standards of input for the
        // builder.
        ULocale userLocale =
                new ULocale.Builder()
                        .setRegion(getResources().getConfiguration().getLocales().get(0).getCountry())
                        .setLanguage(getResources().getConfiguration().getLocales().get(0).getLanguage())
                        .build();
        for (int i = 0; i < keys.length; i++) {
            ULocale settingRowDisplayCountry = new Builder().setRegion(values[i].toString()).build();
            String localizedDisplayCountry = settingRowDisplayCountry.getDisplayCountry(userLocale);
            String settingDisplayName = localizedDisplayCountry + " " + keys[i];
            displayNamesandCountryCodes.add(
                    DisplayNameAndCountryCodeTuple.create(settingDisplayName, values[i]));
        }

        return displayNamesandCountryCodes;
    }

    boolean updateListSummary(Preference pref, Object newValue) {
        ListPreference listPref = (ListPreference) pref;
        CharSequence[] entries = listPref.getEntries();
        listPref.setSummary(entries[listPref.findIndexOfValue(newValue.toString())]);
        return true;
    }

    boolean logIfUserDisabledFeature(Preference pref, Object newValue) {
        if (!((boolean) newValue)) {
            Logger.get(getActivity().getApplicationContext())
                    .logImpression(DialerImpression.Type.ASSISTED_DIALING_FEATURE_DISABLED_BY_USER);
        }

        return true;
    }

    @AutoValue
    abstract static class DisplayNameAndCountryCodeTuple {

        static DisplayNameAndCountryCodeTuple create(
                CharSequence countryDisplayName, CharSequence countryCode) {
            return new AutoValue_AssistedDialingSettingFragment_DisplayNameAndCountryCodeTuple(
                    countryDisplayName, countryCode);
        }

        // The user-readable name of the country.
        abstract CharSequence countryDisplayname();

        // The ISO 3166-2 country code of the country.
        abstract CharSequence countryCode();
    }
}

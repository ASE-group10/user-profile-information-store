package nl.ase_wayfinding.user_profile_information_store.responses;

import java.util.List;

public class NearbyUsersResponse {
    private List<String> phoneNumbers;

    public NearbyUsersResponse(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }
    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}

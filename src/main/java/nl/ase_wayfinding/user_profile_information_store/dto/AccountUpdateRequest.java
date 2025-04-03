package nl.ase_wayfinding.user_profile_information_store.dto;

import lombok.Data;

@Data
public class AccountUpdateRequest {
    private String name;
    private String password;
    private String picture;
}

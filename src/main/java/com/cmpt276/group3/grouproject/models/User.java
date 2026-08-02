package com.cmpt276.group3.grouproject.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Size(max=20)
    private String first_name;
    @Size(max=30)
    private String last_name;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Nullable
    private String avatar;

    @Nullable
    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    @Nullable
    @Column(name = "avatar_data", length = 2097152)
    private byte[] avatarData;

    @Nullable
    @Size(max = 100)
    @Column(name = "avatar_content_type", length = 100)
    private String avatarContentType;

    @Nullable
    private Boolean isCAS = false;

    public User() {

    }

    public User(String first_name, String last_name, String email, String password, Role role, Gender gender, String avatar) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.gender = gender;
        this.avatar = avatar;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        if (hasUploadedAvatar() && id > 0) {
            return "/users/" + id + "/avatar";
        }

        if (avatar == null || avatar.isBlank()) {
            return "/user.png";
        }

        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public byte[] getAvatarData() {
        return avatarData;
    }

    public void setAvatarData(byte[] avatarData) {
        this.avatarData = avatarData;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    public boolean hasUploadedAvatar() {
        return avatarData != null
            && avatarData.length > 0
            && avatarContentType != null
            && !avatarContentType.isBlank();
    }

    public boolean isAdmin() {
        return role==Role.ADMIN;
    }

    public boolean isMod() {
        return role==Role.MOD;
    }

    public Boolean isCAS() {
        if (isCAS==null) return false;
        return isCAS;
    }

    public void setCAS(boolean isCAS) {
        this.isCAS = isCAS;
    }
}

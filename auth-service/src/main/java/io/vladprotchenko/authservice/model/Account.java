package io.vladprotchenko.authservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@FilterDef(name = Account.FILTER_ACTIVE,
    autoEnabled = true,
    applyToLoadByKey = true,
    defaultCondition = "is_soft_deleted = 'false'")

@Filter(name = Account.FILTER_ACTIVE)
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Account {

    public static final String FILTER_ACTIVE = "Active";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID accountId = UUID.randomUUID();

    private String email;
    private String passwordHash;

    @Column(nullable = false)
    private boolean isSoftDeleted = false;

    @JsonManagedReference
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private TemporaryPassword temporaryPassword;

    private Instant blockedUntil;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @JsonManagedReference
    @OneToOne(mappedBy = "account", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private RefreshToken refreshToken;

    @ManyToOne()
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}

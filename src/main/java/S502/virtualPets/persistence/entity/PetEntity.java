package S502.virtualPets.persistence.entity;

import S502.virtualPets.persistence.enums.MoodEnum;
import S502.virtualPets.persistence.enums.PetTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pets")
public class PetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The name of the pet cannot be empty.")
    @Size(max = 50, message = "The pet's name cannot exceed 50 characters.")
    @Column(nullable = false, length = 50)
    private String name;

    @NotNull(message = "The type of the pet cannot be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private PetTypeEnum petType = PetTypeEnum.SAN_BERNARDO;

    @NotNull(message = "The mascot's encouragement cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MoodEnum mood = MoodEnum.HAPPY;

    @NotNull(message = "The energy level cannot be null.")
    @Min(value = 0, message = "The energy level cannot be less than 0.")
    @Max(value = 100, message = "The energy level cannot be less than 100.")
    @Column(nullable = false)
    @Builder.Default
    private Integer energyLevel = 100;

    @NotNull(message = "The level of hunger cannot be null.")
    @Min(value = 0, message = "The energy level cannot be less than 0.")
    @Max(value = 100, message = "The energy level cannot be greater than 100.")
    @Column(nullable = false)
    @Builder.Default
    private Integer hungerLevel = 0;

    @NotNull(message = "The pet must be associated with a user.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

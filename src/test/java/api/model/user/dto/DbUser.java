package api.model.user.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="customers")
@Data //Nó sẽ tạo các get method giống lombok, ko có set()
public class DbUser {
    @Id //cho nó biết là mình đã có unique identifier
//    @GeneratedValue // tìm hiểu thêm
    private UUID id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String phone;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}

package apiCustomerManagement.model.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="customers")
@Data //Nó sẽ tạo các get method giống lombok, setter và getter
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
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;
}

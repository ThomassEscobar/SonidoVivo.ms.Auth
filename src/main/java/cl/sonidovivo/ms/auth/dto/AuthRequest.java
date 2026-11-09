package cl.sonidovivo.ms.auth.dto;


import lombok.Data;

@Data
public class AuthRequest {
    private String correo;
    private String password;
    private String nombre; // Solo se usa en el registro
}

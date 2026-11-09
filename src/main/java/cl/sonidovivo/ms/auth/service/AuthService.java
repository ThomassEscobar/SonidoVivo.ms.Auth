package cl.sonidovivo.ms.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cl.sonidovivo.ms.auth.dto.AuthRequest;
import cl.sonidovivo.ms.auth.dto.AuthResponse;
import cl.sonidovivo.ms.auth.model.Rol;
import cl.sonidovivo.ms.auth.model.Usuario;
import cl.sonidovivo.ms.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // Lombok crea el constructor para inyectar dependencias
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // 1. Método para Registrar un Usuario
    public AuthResponse register(AuthRequest request) {
        // Verificamos si el correo ya está registrado
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está en uso");
        }

        // Creamos el usuario y encriptamos su contraseña
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.USER); // Por defecto, todos son USER

        usuarioRepository.save(usuario);

        // Generamos el token JWT para que quede logueado automáticamente
        String jwtToken = jwtService.generateToken(usuario);
        return new AuthResponse(jwtToken);
    }

    // 2. Método para Iniciar Sesión
    public AuthResponse login(AuthRequest request) {
        // Spring Security verifica si el correo y contraseña coinciden
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreo(),
                        request.getPassword()
                )
        );

        // Si pasa la verificación, buscamos al usuario y generamos su token
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(usuario);
        return new AuthResponse(jwtToken);
    }
}

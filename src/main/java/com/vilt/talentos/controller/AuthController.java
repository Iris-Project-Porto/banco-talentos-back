@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest req) {
        log.info("Tentando login para: {}", req.email());
        
        var user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", req.email());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        log.info("Usuário encontrado: {} | role: {}", user.getEmail(), user.getRole());
        log.info("Senha recebida: {} | Hash no banco: {}", req.password(), user.getPassword());
        
        boolean matches = passwordEncoder.matches(req.password(), user.getPassword());
        log.info("Senha confere: {}", matches);
        
        if (!matches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generate(user.getId().toString(), Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }
}
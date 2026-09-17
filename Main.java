import java.util.*;
import java.util.stream.Collectors;

public class Main {


    public static class User {
        private String id;
        private String name;
        private String email;

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return String.format("User[ID='%s', Nombre='%s', Email='%s']", id, name, email);
        }
    }

    public static class Aeropuerto {
        private String codigoIata; 
        private String nombre;
        private String ciudad;
        private String pais;
        private String continente;

        public Aeropuerto(String codigoIata, String nombre, String ciudad, String pais, String continente) {
            this.codigoIata = codigoIata != null ? codigoIata.toUpperCase() : null;
            this.nombre = nombre;
            this.ciudad = ciudad;
            this.pais = pais;
            this.continente = continente;
        }

        public String getCodigoIata() { return codigoIata; }
        public String getNombre() { return nombre; }
        public String getCiudad() { return ciudad; }
        public String getPais() { return pais; }
        public String getContinente() { return continente; }

        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setCiudad(String ciudad) { this.ciudad = ciudad; }
        public void setPais(String pais) { this.pais = pais; }
        public void setContinente(String continente) { this.continente = continente; }

        @Override
        public String toString() {
            return String.format("Aeropuerto [IATA: %-4s | Nombre: %-30s | Ciudad: %-15s | País: %-12s | Continente: %-10s]",
                    codigoIata, nombre, ciudad, pais, continente);
        }
    }

  
    public interface UserRepositoryPort {
        User save(User user);
        Optional<User> findById(String id);
        List<User> findAll();
    }

    public interface AeropuertoRepositoryPort {
        Aeropuerto save(Aeropuerto aeropuerto);
        Optional<Aeropuerto> findByCodigoIata(String codigoIata);
        List<Aeropuerto> findAll();
        boolean update(Aeropuerto aeropuerto);
        boolean deleteByCodigoIata(String codigoIata);
    }


    public interface AeropuertoUseCase {
        Aeropuerto crearAeropuerto(String codigoIata, String nombre, String ciudad, String pais, String continente);
        Optional<Aeropuerto> buscarAeropuerto(String codigoIata);
        List<Aeropuerto> listarAeropuertos();
        boolean actualizarAeropuerto(String codigoIata, String nombre, String ciudad, String pais, String continente);
        boolean eliminarAeropuerto(String codigoIata);
    }


    public static class AeropuertoUseCaseImpl implements AeropuertoUseCase {
        private final AeropuertoRepositoryPort repository;

        public AeropuertoUseCaseImpl(AeropuertoRepositoryPort repository) {
            this.repository = repository;
        }

        @Override
        public Aeropuerto crearAeropuerto(String codigoIata, String nombre, String ciudad, String pais, String continente) {
            if (codigoIata == null || codigoIata.trim().length() != 3) {
                throw new IllegalArgumentException("El código IATA debe tener exactamente 3 letras.");
            }
            if (repository.findByCodigoIata(codigoIata).isPresent()) {
                throw new IllegalStateException("Ya existe un aeropuerto con el código IATA: " + codigoIata);
            }
            Aeropuerto aeropuerto = new Aeropuerto(codigoIata, nombre, ciudad, pais, continente);
            return repository.save(aeropuerto);
        }

        @Override
        public Optional<Aeropuerto> buscarAeropuerto(String codigoIata) {
            return repository.findByCodigoIata(codigoIata);
        }

        @Override
        public List<Aeropuerto> listarAeropuertos() {
            return repository.findAll();
        }

        @Override
        public boolean actualizarAeropuerto(String codigoIata, String nombre, String ciudad, String pais, String continente) {
            Optional<Aeropuerto> existente = repository.findByCodigoIata(codigoIata);
            if (existente.isPresent()) {
                Aeropuerto a = existente.get();
                if (nombre != null && !nombre.isBlank()) a.setNombre(nombre);
                if (ciudad != null && !ciudad.isBlank()) a.setCiudad(ciudad);
                if (pais != null && !pais.isBlank()) a.setPais(pais);
                if (continente != null && !continente.isBlank()) a.setContinente(continente);
                return repository.update(a);
            }
            return false;
        }

        @Override
        public boolean eliminarAeropuerto(String codigoIata) {
            return repository.deleteByCodigoIata(codigoIata);
        }
    }

    public static class InMemoryUserRepositoryAdapter implements UserRepositoryPort {
        private final Map<String, User> db = new HashMap<>();

        @Override
        public User save(User user) {
            db.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(String id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(db.values());
        }
    }

    // --- Adaptador de Memoria para Aeropuerto (CRUDL Completo) ---
    public static class InMemoryAeropuertoAdapter implements AeropuertoRepositoryPort {
        private final Map<String, Aeropuerto> storage = new HashMap<>();

        public InMemoryAeropuertoAdapter() {
            // Carga inicial de datos de prueba del CEA
            save(new Aeropuerto("CTG", "Rafael Núñez", "Cartagena", "Colombia", "Suramérica"));
            save(new Aeropuerto("BOG", "El Dorado", "Bogotá", "Colombia", "Suramérica"));
            save(new Aeropuerto("MIA", "Miami International", "Miami", "EE.UU.", "Norteamérica"));
            save(new Aeropuerto("MAD", "Adolfo Suárez Barajas", "Madrid", "España", "Europa"));
        }

        @Override
        public Aeropuerto save(Aeropuerto aeropuerto) {
            storage.put(aeropuerto.getCodigoIata().toUpperCase(), aeropuerto);
            return aeropuerto;
        }

        @Override
        public Optional<Aeropuerto> findByCodigoIata(String codigoIata) {
            if (codigoIata == null) return Optional.empty();
            return Optional.ofNullable(storage.get(codigoIata.toUpperCase()));
        }

        @Override
        public List<Aeropuerto> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public boolean update(Aeropuerto aeropuerto) {
            if (storage.containsKey(aeropuerto.getCodigoIata().toUpperCase())) {
                storage.put(aeropuerto.getCodigoIata().toUpperCase(), aeropuerto);
                return true;
            }
            return false;
        }

        @Override
        public boolean deleteByCodigoIata(String codigoIata) {
            if (codigoIata == null) return false;
            return storage.remove(codigoIata.toUpperCase()) != null;
        }
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Inicialización de Repositorios y Casos de Uso
        UserRepositoryPort userRepo = new InMemoryUserRepositoryAdapter();
        AeropuertoRepositoryPort aeropuertoRepo = new InMemoryAeropuertoAdapter();
        AeropuertoUseCase aeropuertoUseCase = new AeropuertoUseCaseImpl(aeropuertoRepo);


        userRepo.save(new User("1", "Juan Romero", "juan@unicartagena.edu.co"));

        boolean salir = false;
        System.out.println("=================================================================");
        System.out.println("  SISTEMA DE CONTROL DE VUELOS Y RESERVAS (CEA - UNICARTAGENA)  ");
        System.out.println("  Arquitectura Hexagonal (DDD) - Gestión de Aeropuertos y Users ");
        System.out.println("=================================================================");

        while (!salir) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. [User] Crear Usuario");
            System.out.println("2. [User] Listar Usuarios");
            System.out.println("3. [Aeropuerto - C] Crear Aeropuerto");
            System.out.println("4. [Aeropuerto - R] Buscar Aeropuerto por Código IATA");
            System.out.println("5. [Aeropuerto - U] Actualizar Aeropuerto");
            System.out.println("6. [Aeropuerto - D] Eliminar Aeropuerto");
            System.out.println("7. [Aeropuerto - L] Listar todos los Aeropuertos");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine().trim();

            try {
                switch (opcion) {
                    case "1":
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Nombre: "); String name = scanner.nextLine();
                        System.out.print("Email: "); String email = scanner.nextLine();
                        userRepo.save(new User(id, name, email));
                        System.out.println(" Usuario registrado con éxito.");
                        break;

                    case "2":
                        System.out.println("\n--- LISTA DE USUARIOS ---");
                        userRepo.findAll().forEach(System.out::println);
                        break;

                    case "3":
                        System.out.print("Código IATA (3 letras, ej: CTG): "); String iata = scanner.nextLine();
                        System.out.print("Nombre Aeropuerto: "); String nom = scanner.nextLine();
                        System.out.print("Ciudad: "); String ciu = scanner.nextLine();
                        System.out.print("País: "); String pais = scanner.nextLine();
                        System.out.print("Continente: "); String cont = scanner.nextLine();
                        Aeropuerto creado = aeropuertoUseCase.crearAeropuerto(iata, nom, ciu, pais, cont);
                        System.out.println(" Aeropuerto registrado con éxito: " + creado);
                        break;

                    case "4":
                        System.out.print("Ingrese Código IATA a buscar: "); String iataBusqueda = scanner.nextLine();
                        Optional<Aeropuerto> encontrado = aeropuertoUseCase.buscarAeropuerto(iataBusqueda);
                        if (encontrado.isPresent()) {
                            System.out.println(" Aeropuerto encontrado: " + encontrado.get());
                        } else {
                            System.out.println(" No se encontró ningún aeropuerto con el código " + iataBusqueda);
                        }
                        break;

                    case "5":
                        System.out.print("Ingrese Código IATA del Aeropuerto a actualizar: "); String iataUpd = scanner.nextLine();
                        System.out.print("Nuevo Nombre (dejar en blanco para no cambiar): "); String nNom = scanner.nextLine();
                        System.out.print("Nueva Ciudad (dejar en blanco para no cambiar): "); String nCiu = scanner.nextLine();
                        System.out.print("Nuevo País (dejar en blanco para no cambiar): "); String nPais = scanner.nextLine();
                        System.out.print("Nuevo Continente (dejar en blanco para no cambiar): "); String nCont = scanner.nextLine();
                        boolean exitoUpd = aeropuertoUseCase.actualizarAeropuerto(iataUpd, nNom, nCiu, nPais, nCont);
                        if (exitoUpd) {
                            System.out.println(" Aeropuerto actualizado correctamente.");
                        } else {
                            System.out.println(" No se pudo actualizar. Verifique el código IATA.");
                        }
                        break;

                    case "6":
                        System.out.print("Ingrese Código IATA del Aeropuerto a eliminar: "); String iataDel = scanner.nextLine();
                        boolean exitoDel = aeropuertoUseCase.eliminarAeropuerto(iataDel);
                        if (exitoDel) {
                            System.out.println(" Aeropuerto eliminado correctamente.");
                        } else {
                            System.out.println(" No se encontró el aeropuerto a eliminar.");
                        }
                        break;

                    case "7":
                        System.out.println("\n--- LISTADO DE AEROPUERTOS (CRUDL - LIST) ---");
                        List<Aeropuerto> lista = aeropuertoUseCase.listarAeropuertos();
                        if (lista.isEmpty()) {
                            System.out.println("No hay aeropuertos registrados.");
                        } else {
                            lista.forEach(a -> System.out.println("  • " + a));
                        }
                        break;

                    case "0":
                        salir = true;
                        System.out.println("Cerrando la aplicación. ¡Gracias!");
                        break;

                    default:
                        System.out.println(" Opción inválida. Intente de nuevo.");
                }
            } catch (Exception e) {
                System.out.println(" ERROR: " + e.getMessage());
            }
        }
        scanner.close();
    }
}
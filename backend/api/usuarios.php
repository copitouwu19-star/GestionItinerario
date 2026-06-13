<?php
// ============================================================
// API REST: usuarios
// URL base: http://localhost/soluciones/api/usuarios.php
// ============================================================
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit; }

require_once __DIR__ . '/../config/database.php';

$method = $_SERVER['REQUEST_METHOD'];
$conn   = getConnection();

switch ($method) {

    // ----------------------------------------------------------
    // GET /usuarios.php              → listar todos
    // GET /usuarios.php?id=X         → por ID
    // GET /usuarios.php?firebase_uid=X → por UID de Firebase
    // ----------------------------------------------------------
    case 'GET':
        if (!empty($_GET['firebase_uid'])) {
            $uid  = $conn->real_escape_string($_GET['firebase_uid']);
            $res  = $conn->query("SELECT * FROM usuarios WHERE firebase_uid = '$uid' LIMIT 1");
            $row  = $res->fetch_assoc();
            if ($row) {
                echo json_encode(['success' => true, 'data' => $row]);
            } else {
                http_response_code(404);
                echo json_encode(['success' => false, 'message' => 'Usuario no encontrado']);
            }
        } elseif (!empty($_GET['id'])) {
            $id  = (int) $_GET['id'];
            $res = $conn->query("SELECT * FROM usuarios WHERE id = $id LIMIT 1");
            $row = $res->fetch_assoc();
            if ($row) {
                echo json_encode(['success' => true, 'data' => $row]);
            } else {
                http_response_code(404);
                echo json_encode(['success' => false, 'message' => 'Usuario no encontrado']);
            }
        } else {
            $res  = $conn->query("SELECT * FROM usuarios ORDER BY fecha_creacion DESC");
            $rows = [];
            while ($row = $res->fetch_assoc()) { $rows[] = $row; }
            echo json_encode(['success' => true, 'data' => $rows]);
        }
        break;

    // ----------------------------------------------------------
    // POST /usuarios.php   → crear usuario
    // Body: { firebase_uid, nombre, apellido, email, telefono, rol, empresa }
    // ----------------------------------------------------------
    case 'POST':
        $body = json_decode(file_get_contents('php://input'), true);
        if (empty($body['firebase_uid']) || empty($body['nombre']) || empty($body['email'])) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Faltan campos obligatorios: firebase_uid, nombre, email']);
            break;
        }

        $uid      = $conn->real_escape_string($body['firebase_uid']);
        $nombre   = $conn->real_escape_string($body['nombre']);
        $apellido = $conn->real_escape_string($body['apellido'] ?? '');
        $email    = $conn->real_escape_string($body['email']);
        $telefono = $conn->real_escape_string($body['telefono'] ?? '');
        $rol      = in_array($body['rol'] ?? '', ['admin', 'tecnico', 'cliente']) ? $body['rol'] : 'tecnico';
        $empresa  = $conn->real_escape_string($body['empresa'] ?? '');

        $sql = "INSERT INTO usuarios (firebase_uid, nombre, apellido, email, telefono, rol, empresa, ultimo_acceso)
                VALUES ('$uid', '$nombre', '$apellido', '$email', '$telefono', '$rol', '$empresa', NOW())
                ON DUPLICATE KEY UPDATE
                    nombre        = VALUES(nombre),
                    apellido      = VALUES(apellido),
                    telefono      = VALUES(telefono),
                    rol           = VALUES(rol),
                    empresa       = VALUES(empresa),
                    ultimo_acceso = NOW()";

        if ($conn->query($sql)) {
            $newId = $conn->insert_id ?: null;
            $res   = $conn->query("SELECT * FROM usuarios WHERE firebase_uid = '$uid' LIMIT 1");
            $row   = $res->fetch_assoc();
            http_response_code(201);
            echo json_encode(['success' => true, 'message' => 'Usuario guardado', 'data' => $row]);
        } else {
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Error al guardar: ' . $conn->error]);
        }
        break;

    // ----------------------------------------------------------
    // PUT /usuarios.php?id=X   → actualizar usuario
    // ----------------------------------------------------------
    case 'PUT':
        $id = (int) ($_GET['id'] ?? 0);
        if (!$id) { http_response_code(400); echo json_encode(['success' => false, 'message' => 'Se requiere ?id=X']); break; }

        $body     = json_decode(file_get_contents('php://input'), true);
        $campos   = [];
        if (isset($body['nombre']))   $campos[] = "nombre   = '" . $conn->real_escape_string($body['nombre'])   . "'";
        if (isset($body['apellido'])) $campos[] = "apellido = '" . $conn->real_escape_string($body['apellido']) . "'";
        if (isset($body['telefono'])) $campos[] = "telefono = '" . $conn->real_escape_string($body['telefono']) . "'";
        if (isset($body['empresa']))  $campos[] = "empresa  = '" . $conn->real_escape_string($body['empresa'])  . "'";
        if (isset($body['rol']) && in_array($body['rol'], ['admin','tecnico','cliente'])) $campos[] = "rol = '{$body['rol']}'";
        if (isset($body['activo']))   $campos[] = "activo   = " . ((int) $body['activo']);
        $campos[] = "ultimo_acceso = NOW()";

        $conn->query("UPDATE usuarios SET " . implode(', ', $campos) . " WHERE id = $id");
        $res = $conn->query("SELECT * FROM usuarios WHERE id = $id LIMIT 1");
        echo json_encode(['success' => true, 'message' => 'Usuario actualizado', 'data' => $res->fetch_assoc()]);
        break;

    // ----------------------------------------------------------
    // DELETE /usuarios.php?id=X  → desactivar (soft delete)
    // ----------------------------------------------------------
    case 'DELETE':
        $id = (int) ($_GET['id'] ?? 0);
        if (!$id) { http_response_code(400); echo json_encode(['success' => false, 'message' => 'Se requiere ?id=X']); break; }

        $conn->query("UPDATE usuarios SET activo = 0 WHERE id = $id");
        echo json_encode(['success' => true, 'message' => 'Usuario desactivado']);
        break;

    default:
        http_response_code(405);
        echo json_encode(['success' => false, 'message' => 'Método no permitido']);
}

$conn->close();

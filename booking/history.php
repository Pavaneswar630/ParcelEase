<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include "../dbconnect.php"; // Ensure dbconnect.php is correctly set up

// Ensure request method is POST
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

// Get user_id from POST input
$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(["status" => "error", "message" => "Invalid user ID"]);
    exit;
}

$response = [];

// ---------- Fetch Parcel Orders ----------
$parcel_query = "SELECT parcel_id, created_at, status, pickup_location AS from_location, drop_location AS to_location, weight, package_type, amount, thing_type FROM parcels WHERE user_id = ? ORDER BY created_at DESC";
$parcel_stmt = $conn->prepare($parcel_query);
$parcel_stmt->bind_param("i", $user_id);
$parcel_stmt->execute();
$parcel_result = $parcel_stmt->get_result();

$parcels = [];
while ($row = $parcel_result->fetch_assoc()) {
    $row['type'] = 'parcel';
    $parcels[] = $row;
}
$parcel_stmt->close();
// ---------- Fetch Truck Bookings ----------
$truck_query = "SELECT * FROM truck_bookings WHERE user_id = ? ORDER BY created_at DESC";
$truck_stmt = $conn->prepare($truck_query);
$truck_stmt->bind_param("i", $user_id);
$truck_stmt->execute();
$truck_result = $truck_stmt->get_result();

$trucks = [];
while ($row = $truck_result->fetch_assoc()) {
    $row['type'] = 'truck';
    $trucks[] = $row;
}
$truck_stmt->close();

// ---------- Fetch In-City Bookings ----------
$incity_query = "SELECT * FROM incitybookings WHERE user_id = ? ORDER BY created_at DESC";
$incity_stmt = $conn->prepare($incity_query);
$incity_stmt->bind_param("i", $user_id);
$incity_stmt->execute();
$incity_result = $incity_stmt->get_result();

$incity_bookings = [];
while ($row = $incity_result->fetch_assoc()) {
    $row['type'] = 'incity';
    $incity_bookings[] = $row;
}
$incity_stmt->close();

// ---------- Combine and Respond ----------
if (!empty($parcels) || !empty($trucks) || !empty($incity_bookings)) {
    echo json_encode([
        "status" => "success",
        "parcels" => $parcels,
        "trucks" => $trucks,
        "incity" => $incity_bookings
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "No booking history found"]);
}

$conn->close();
?>
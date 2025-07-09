<?php
include "../dbconnect.php";
header('Content-Type: application/json');

$user_id = $_GET['user_id'] ?? $_POST['user_id'] ?? '';

if (empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "User ID is required"]);
    exit();
}

$activities = [];

// Fetch recent parcel bookings
$sql = "SELECT parcel_id, created_at FROM parcels WHERE user_id = ? ORDER BY created_at DESC LIMIT 5";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $activities[] = [
        "type" => "parcel",
        "shipment_id" => $row["parcel_id"],
        "timestamp" => $row["created_at"],
        "display_time" => formatTimeAgo($row["created_at"]),
        "icon" => "ic_parcel",
        "title" => "Parcel Booking"
    ];
}
$stmt->close();

// Fetch recent in-city bookings
$sql = "SELECT parcel_id, created_at FROM incitybookings WHERE user_id = ? ORDER BY created_at DESC LIMIT 5";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $activities[] = [
        "type" => "incity",
        "shipment_id" => $row["parcel_id"],
        "timestamp" => $row["created_at"],
        "display_time" => formatTimeAgo($row["created_at"]),
        "icon" => "ic_incity",
        "title" => "In-City Booking"
    ];
}
$stmt->close();

// Fetch recent truck bookings
$sql = "SELECT id, created_at FROM truck_bookings WHERE user_id = ? ORDER BY created_at DESC LIMIT 5";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $activities[] = [
        "type" => "truck",
        "shipment_id" => "TR" . str_pad($row["id"], 5, "0", STR_PAD_LEFT),
        "timestamp" => $row["created_at"],
        "display_time" => formatTimeAgo($row["created_at"]),
        "icon" => "ic_truck",
        "title" => "Truck Booking"
    ];
}
$stmt->close();
$conn->close();

// Sort by newest (based on raw timestamp)
usort($activities, function($a, $b) {
    return strtotime($b["timestamp"]) - strtotime($a["timestamp"]);
});

// Replace timestamp with display_time
foreach ($activities as &$a) {
    $a["timestamp"] = $a["display_time"];
    unset($a["display_time"]);
}

echo json_encode(["status" => "success", "activities" => $activities]);

// Format helper
function formatTimeAgo($datetime) {
    $time = strtotime($datetime);
    $diff = time() - $time;

    if ($diff < 60) return $diff . " sec ago";
    if ($diff < 3600) return floor($diff / 60) . " min ago";
    if ($diff < 86400) return floor($diff / 3600) . "h ago";
    return date("d M, Y", $time);
}
?>

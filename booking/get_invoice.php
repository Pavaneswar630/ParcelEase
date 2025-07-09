<?php
require('../includes/fpdf/fpdf.php');
require '../vendor/autoload.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

include "../dbconnect.php";

function fetchParcelDetails($conn, $parcel_id) {
    $query = "SELECT p.parcel_id, p.pickup_location, p.drop_location, p.weight, p.status, p.created_at, 
                     pay.amount, pay.payment_method, pay.transaction_id, 
                     u.name, u.email, u.phone, u.address 
              FROM parcels p
              JOIN payments pay ON p.parcel_id = pay.parcel_id
              JOIN users u ON p.user_id = u.id
              WHERE p.parcel_id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $parcel_id);
    $stmt->execute();
    return $stmt->get_result()->fetch_assoc();
}

function generatePDF($parcel, $outputMode = 'I', $filename = '') {
    $pdf = new FPDF();
    $pdf->AddPage();
    $pdf->SetFont('Arial', 'B', 16);
    $pdf->Cell(190, 10, 'ParcelEase - Invoice', 0, 1, 'C');
    $pdf->SetFont('Arial', '', 12);
    $pdf->Cell(190, 10, 'Date: ' . date('Y-m-d H:i:s'), 0, 1, 'C');
    $pdf->Ln(10);

    // Customer Info
    $pdf->SetFont('Arial', 'B', 12);
    $pdf->Cell(100, 10, 'Customer Details:', 0, 1);
    $pdf->SetFont('Arial', '', 12);
    $pdf->Cell(100, 8, "Name: " . $parcel['name'], 0, 1);
    $pdf->Cell(100, 8, "Email: " . $parcel['email'], 0, 1);
    $pdf->Cell(100, 8, "Phone: " . $parcel['phone'], 0, 1);
    $pdf->Cell(100, 8, "Address: " . $parcel['address'], 0, 1);
    $pdf->Ln(10);

    // Parcel Info
    $pdf->SetFont('Arial', 'B', 12);
    $pdf->Cell(100, 10, 'Parcel Details:', 0, 1);
    $pdf->SetFont('Arial', '', 12);
    $pdf->Cell(100, 8, "Parcel ID: " . $parcel['parcel_id'], 0, 1);
    $pdf->Cell(100, 8, "Weight: " . $parcel['weight'] . " kg", 0, 1);
    $pdf->Cell(100, 8, "Status: " . $parcel['status'], 0, 1);
    $pdf->Cell(100, 8, "Created At: " . $parcel['created_at'], 0, 1);
    $pdf->Ln(10);

    // Shipment Info
    $pdf->SetFont('Arial', 'B', 12);
    $pdf->Cell(100, 10, 'Shipment Details:', 0, 1);
    $pdf->SetFont('Arial', '', 12);
    $pdf->Cell(100, 8, "Pickup Location: " . $parcel['pickup_location'], 0, 1);
    $pdf->Cell(100, 8, "Drop Location: " . $parcel['drop_location'], 0, 1);
    $pdf->Ln(10);

    // Payment Info
    $pdf->SetFont('Arial', 'B', 12);
    $pdf->Cell(100, 10, 'Payment Details:', 0, 1);
    $pdf->SetFont('Arial', '', 12);
    $pdf->Cell(100, 8, "Transaction ID: " . $parcel['transaction_id'], 0, 1);
    $pdf->Cell(100, 8, "Payment Method: " . $parcel['payment_method'], 0, 1);
    $pdf->Cell(100, 8, "Amount Paid: ₹" . number_format($parcel['amount'], 2), 0, 1);
    $pdf->Ln(10);

    // Signature
    $pdf->SetFont('Arial', 'B', 12);
    $pdf->Cell(190, 10, 'Thank You for Using ParcelEase!', 0, 1, 'C');
    $pdf->Ln(10);
    $pdf->Cell(100, 8, 'Authorized Signature: __________________', 0, 1);

    if ($outputMode === 'F') {
        $pdf->Output('F', $filename);
    } else {
        $pdf->Output('I', "Invoice_{$parcel['parcel_id']}.pdf");
    }
}

// === Handle POST (Send email) ===
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['parcel_id'])) {
    $parcel_id = $_POST['parcel_id'];
    $parcel = fetchParcelDetails($conn, $parcel_id);

    if (!$parcel) {
        echo json_encode(["status" => "error", "message" => "Parcel not found"]);
        exit;
    }

    // Temp file
    $filename = "Invoice_{$parcel_id}.pdf";
    $tempPath = sys_get_temp_dir() . DIRECTORY_SEPARATOR . $filename;
    generatePDF($parcel, 'F', $tempPath);

    // Email it
    $mail = new PHPMailer(true);
    try {
        $mail->isSMTP();
    $mail->Host = 'smtp.gmail.com'; // Change to your SMTP server
    $mail->SMTPAuth = true;
    $mail->Username = 'pavaneswar224@gmail.com'; // Change to your email
    $mail->Password = 'azmc qwff lwxe pnvu'; // Use App Password if using Gmail
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
    $mail->Port = 587;

        $mail->setFrom('your_email@gmail.com', 'ParcelEase Invoices');
        $mail->addAddress($parcel['email'], $parcel['name']);

        $mail->isHTML(true);
        $mail->Subject = 'Your ParcelEase Invoice';
        $mail->Body = "Dear {$parcel['name']},<br><br>Please find your invoice attached.<br><br>Thanks for using ParcelEase.";
        $mail->addAttachment($tempPath, $filename);

        $mail->send();
        unlink($tempPath);
        echo json_encode(["status" => "success", "message" => "Invoice sent to email"]);
    } catch (Exception $e) {
        echo json_encode(["status" => "error", "message" => "Mailer Error: " . $mail->ErrorInfo]);
    }
    exit;
}

// === Handle GET (Display PDF) ===
if ($_SERVER['REQUEST_METHOD'] === 'GET' && isset($_GET['parcel_id'])) {
    $parcel_id = $_GET['parcel_id'];
    $parcel = fetchParcelDetails($conn, $parcel_id);

    if (!$parcel) {
        die("Parcel not found.");
    }

    header('Content-Type: application/pdf');
    generatePDF($parcel, 'I');
    exit;
}

echo json_encode(["status" => "error", "message" => "Invalid request"]);
?>

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BankingWebApp {

    private static double balance = 1000.0;
    private static final String LOGIN_USER = "bank";
    private static final String LOGIN_PASS = "123";
    private static final String ACCOUNT_NUMBER = "1234567890";
    private static final String PIN = "123321";
    private static boolean isLoggedIn = false;

    private static final List<String> transactionHistory = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new LoginHandler());
        server.createContext("/home", new HomeHandler());
        server.createContext("/verify", new VerificationHandler());
        server.createContext("/balance", new BalanceHandler());
        server.createContext("/deposit", new DepositHandler());
        server.createContext("/withdraw", new WithdrawHandler());
        server.createContext("/history", new HistoryHandler());
        server.createContext("/pay", new PayHandler()); // Add pay context

        server.setExecutor(null);
        server.start();
        System.out.println("✅ Server started at http://localhost:8080/");
    }

    // Login Page
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String username = getParam(body, "username");
                String password = getParam(body, "password");

                if (LOGIN_USER.equals(username) && LOGIN_PASS.equals(password)) {
                    isLoggedIn = true;
                    exchange.getResponseHeaders().add("Location", "/home");
                    exchange.sendResponseHeaders(302, -1);
                    return;
                } else {
                    sendResponse(exchange, pageWrapper("<h1>❌ Invalid Credentials</h1><a href='/'>Try Again</a>"));
                    return;
                }
            }

            String form = """
                <div class='login-container'>
                  <h1>Banking Login</h1>
                  <form method='POST'>
                    <input type='text' name='username' placeholder='Username' required><br>
                    <input type='password' name='password' placeholder='Password' required><br>
                    <button type='submit'>Login</button>
                  </form>
                </div>
                """;
            sendResponse(exchange, pageWrapper(form));
        }
    }

    // Home Page
    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isLoggedIn) {
                exchange.getResponseHeaders().add("Location", "/");
                exchange.sendResponseHeaders(302, -1);
                return;
            }
            String response = """
                <div class='animated fadeInDown'>
                  <h1>🏦 Friendly Banking</h1>
                  <div class='quick-pay animated fadeInUp'>
                    <h2>Quick Pay</h2>
                    <a href='/pay?method=phonepe' class='pay-btn phonepe'>PhonePe</a>
                    <a href='/pay?method=paytm' class='pay-btn paytm'>Paytm</a>
                    <a href='/pay?method=gpay' class='pay-btn gpay'>GPay</a>
                  </div>
                  <div class='menu-links animated fadeInUp' style="display:flex;flex-wrap:wrap;justify-content:center;gap:2.5rem;margin-top:2.5rem;">
                    <a href='/verify?next=balance' class='hover-box'>
                      <div class='hover-box-inner'>
                        <span class='hover-box-icon'>💰</span>
                        <span class='hover-box-title'>Check Balance</span>
                      </div>
                    </a>
                    <a href='/verify?next=deposit' class='hover-box'>
                      <div class='hover-box-inner'>
                        <span class='hover-box-icon'>➕</span>
                        <span class='hover-box-title'>Deposit Money</span>
                      </div>
                    </a>
                    <a href='/verify?next=withdraw' class='hover-box'>
                      <div class='hover-box-inner'>
                        <span class='hover-box-icon'>➖</span>
                        <span class='hover-box-title'>Withdraw Money</span>
                      </div>
                    </a>
                    <a href='/history' class='hover-box'>
                      <div class='hover-box-inner'>
                        <span class='hover-box-icon'>📜</span>
                        <span class='hover-box-title'>View Transaction History</span>
                      </div>
                    </a>
                  </div>
                </div>
                <script>
                  document.querySelectorAll('.hover-box').forEach(function(el) {
                    el.addEventListener('mouseenter', function() {
                      el.classList.add('pulse');
                    });
                    el.addEventListener('mouseleave', function() {
                      el.classList.remove('pulse');
                    });
                  });
                  document.querySelectorAll('.menu-links a, .pay-btn').forEach(function(el) {
                    el.addEventListener('mouseenter', function() {
                      el.classList.add('pulse');
                    });
                    el.addEventListener('mouseleave', function() {
                      el.classList.remove('pulse');
                    });
                  });
                </script>
                """;
            sendResponse(exchange, pageWrapper(response));
        }
    }


    static class PayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String method = "Pay";
            String upiId = "sonyjadda@okicici";
            String payeeName = "Jadda Sony";
            String upiUrl = "";
            String qrApiUrl = "";
            boolean useCustomQR = false;
            if (query != null && query.contains("method=")) {
                String m = query.split("=")[1];
                if ("phonepe".equalsIgnoreCase(m)) {
                    method = "PhonePe";
                    upiUrl = "upi://pay?pa=" + upiId + "&pn=" + payeeName + "&cu=INR";
                    useCustomQR = true;
                } else if ("paytm".equalsIgnoreCase(m)) {
                    method = "Paytm";
                    upiUrl = "upi://pay?pa=" + upiId + "&pn=" + payeeName + "&cu=INR";
                    useCustomQR = true;
                } else if ("gpay".equalsIgnoreCase(m)) {
                    method = "GPay";
                    upiUrl = "upi://pay?pa=" + upiId + "&pn=" + payeeName + "&cu=INR";
                    useCustomQR = true;
                }
                qrApiUrl = "https://chart.googleapis.com/chart?cht=qr&chs=250x250&chl=" + java.net.URLEncoder.encode(upiUrl, "UTF-8");
            }
            String qrImgHtml;
            qrImgHtml = """
                <img src="PHOTO-2025-08-16-22-31-45.jpg" alt="QR Code for %s" style="width:250px;height:250px;border-radius:16px;box-shadow:0 4px 24px #0008;"/>
                <div style='margin-top:0.5rem;font-size:1.1rem;color:#facc15;'>UPI ID: %s</div>
            """.formatted(method, upiId);

            String form = String.format("""
                <div class='animated fadeInDown'>
                  <h2>%s Payment</h2>
                  <form method='POST'>
                    <input type='number' step='0.01' name='amount' placeholder='Enter Amount' required>
                    <button type='submit'>Pay with %s</button>
                  </form>
                  %s
                  <div style='margin:1.5rem 0;'>
                    <h3>Or Scan to Pay with %s</h3>
                    %s
                  </div>
                  <a href='/home'>Back</a>
                </div>
                """,
                method,
                method,
                (upiUrl.isEmpty() ? "" : "<div style='margin-bottom:1rem;color:#34d399;font-size:1.1rem;'>You can also scan the QR code below to pay via UPI apps.</div>"),
                method,
                qrImgHtml
            );

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                double amount = parseAmount(body);
                String result;
                if (amount > 0 && amount <= balance) {
                    balance -= amount;
                    transactionHistory.add("Paid " + amount + " via " + method);
                    result = "<h2 class='animated tada'>✅ Paid " + amount + " via " + method + "</h2>";
                } else if (amount > balance) {
                    result = "<h2 class='animated shakeX'>❌ Insufficient Balance!</h2>";
                } else {
                    result = "<h2 class='animated shakeX'>❌ Invalid Amount!</h2>";
                }
                result += "<a href='/home'>Back</a>";
                sendResponse(exchange, pageWrapper("<div class='animated fadeInDown'>" + result + "</div>"));
            } else {
                sendResponse(exchange, pageWrapper(form));
            }
        }
    }

    // Verification Page (Account & PIN)
    static class VerificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String next = (query != null && query.contains("next=")) ? query.split("=")[1] : "home";

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String account = getParam(body, "account");
                String pin = getParam(body, "pin");

                if (ACCOUNT_NUMBER.equals(account) && PIN.equals(pin)) {
                    exchange.getResponseHeaders().add("Location", "/" + next);
                    exchange.sendResponseHeaders(302, -1);
                    return;
                } else {
                    sendResponse(exchange, pageWrapper("<h1>❌ Invalid Account or PIN</h1><a href='/home'>Back</a>"));
                    return;
                }
            }

            String form = """
                <div class='login-container'>
                  <h2>🔑 Verify Account</h2>
                  <form method='POST'>
                    <input type='text' name='account' placeholder='Account Number' required><br>
                    <input type='password' name='pin' placeholder='PIN' required><br>
                    <button type='submit'>Verify</button>
                  </form>
                </div>
                """;
            sendResponse(exchange, pageWrapper(form));
        }
    }

    // Balance Page
    static class BalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<h1>💰 Current Balance: " + balance + "</h1><a href='/home'>Back</a>";
            transactionHistory.add("Checked Balance: " + balance);
            sendResponse(exchange, pageWrapper(response));
        }
    }

    // Deposit Page
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                double amount = parseAmount(body);
                balance += amount;
                transactionHistory.add("Deposited: +" + amount);
                String response = "<h1>✅ Deposited " + amount + "</h1><a href='/home'>Back</a>";
                sendResponse(exchange, pageWrapper(response));
            } else {
                String form = """
                    <h2>Deposit Money</h2>
                    <form method='POST'>
                      <input type='number' step='0.01' name='amount' placeholder='Enter Amount' required>
                      <button type='submit'>Deposit</button>
                    </form>
                    <a href='/home'>Back</a>
                    """;
                sendResponse(exchange, pageWrapper(form));
            }
        }
    }

    // Withdraw Page
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                double amount = parseAmount(body);
                String response;
                if (amount <= balance) {
                    balance -= amount;
                    transactionHistory.add("Withdrew: -" + amount);
                    response = "<h1>✅ Withdrawn " + amount + "</h1><a href='/home'>Back</a>";
                } else {
                    response = "<h1>❌ Insufficient Balance!</h1><a href='/home'>Back</a>";
                }
                sendResponse(exchange, pageWrapper(response));
            } else {
                String form = """
                    <h2>Withdraw Money</h2>
                    <form method='POST'>
                      <input type='number' step='0.01' name='amount' placeholder='Enter Amount' required>
                      <button type='submit'>Withdraw</button>
                    </form>
                    <a href='/home'>Back</a>
                    """;
                sendResponse(exchange, pageWrapper(form));
            }
        }
    }

    // Transaction History
    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder history = new StringBuilder("<h2>📜 Transaction History</h2><ul>");
            for (String entry : transactionHistory) {
                history.append("<li>").append(entry).append("</li>");
            }
            history.append("</ul><a href='/home'>Back</a>");
            sendResponse(exchange, pageWrapper(history.toString()));
        }
    }

    // Utility to send response
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Netflix-style Page Wrapper with more animation and styles
    private static String pageWrapper(String content) {
        return """
        <html>
        <head>
          <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css"/>
          <style>
            body { margin:0; font-family: Arial, sans-serif; background: linear-gradient(135deg, #141414 0%, #23272f 100%); color: #fff; text-align:center; min-height:100vh; }
            h1, h2 { color: #e50914; }
            a { color: #e50914; text-decoration:none; font-weight:bold; transition: color 0.2s; }
            a:hover { color: #facc15; }
            .login-container { margin:10% auto; width:300px; background:#222; padding:20px; border-radius:10px; box-shadow:0 0 20px #000; }
            input, button { width:90%; padding:10px; margin:10px 0; border:none; border-radius:5px; }
            input { background:#333; color:#fff; }
            button { background:#e50914; color:white; cursor:pointer; font-weight:bold; transition: background 0.2s, transform 0.2s; }
            button:hover { background:#f40612; transform: scale(1.07);}
            ul { list-style:none; padding:0; }
            li { background:#222; margin:5px; padding:10px; border-radius:5px; }
            .animated { animation-duration: 1.1s; }
            .fadeInDown { animation-name: fadeInDown; }
            .fadeInUp { animation-name: fadeInUp; }
            .tada { animation-name: tada; }
            .shakeX { animation-name: shakeX; }
            .pulse { animation: pulse 0.7s; }
            .quick-pay { margin: 2rem 0 1.5rem 0; }
            .pay-btn { display:inline-block; margin:0 0.6rem; padding:0.7rem 1.5rem; border-radius:8px; font-size:1.1rem; font-weight:700; background:#23272f; color:#fff; border:2px solid #e50914; transition: background 0.2s, color 0.2s, transform 0.2s; box-shadow:0 2px 8px #e5091444; }
            .pay-btn.phonepe { border-color:#3b82f6; color:#3b82f6; }
            .pay-btn.paytm { border-color:#0ea5e9; color:#0ea5e9; }
            .pay-btn.gpay { border-color:#34d399; color:#34d399; }
            .pay-btn:hover { background:#facc15; color:#232329; border-color:#facc15; transform: scale(1.09);}
            .menu-links { margin-top:2.5rem; }
            .hover-box {
              display:inline-block;
              background:#23272f;
              border-radius:18px;
              box-shadow:0 2px 16px #0006;
              padding:2.2rem 2.5rem 2.2rem 2.5rem;
              margin:0.5rem 0.5rem;
              min-width:210px;
              min-height:120px;
              text-align:center;
              transition: background 0.2s, color 0.2s, box-shadow 0.2s, transform 0.18s cubic-bezier(.4,0,.2,1);
              color:#fff;
              font-size:1.18rem;
              font-weight:700;
              border:2px solid #232329;
              cursor:pointer;
              position:relative;
            }
            .hover-box:hover, .hover-box:focus {
              background:#facc15;
              color:#232329;
              box-shadow:0 4px 32px #facc1555, 0 0 0 4px #facc1555;
              border-color:#facc15;
              transform: scale(1.09);
              z-index:10;
            }
            .hover-box-inner {
              display:flex;
              flex-direction:column;
              align-items:center;
              justify-content:center;
              gap:0.7rem;
            }
            .hover-box-icon {
              font-size:2.2rem;
              margin-bottom:0.2rem;
              display:block;
            }
            .hover-box-title {
              font-size:1.18rem;
              font-weight:700;
              letter-spacing:0.01em;
              color:inherit;
            }
            /* Animate.css keyframes fallback */
            @keyframes fadeInDown { from { opacity:0; transform:translateY(-40px);} to { opacity:1; transform:translateY(0);} }
            @keyframes fadeInUp { from { opacity:0; transform:translateY(40px);} to { opacity:1; transform:translateY(0);} }
            @keyframes tada { 0%{transform:scale(1);}10%,20%{transform:scale(0.9) rotate(-3deg);}30%,50%,70%,90%{transform:scale(1.1) rotate(3deg);}40%,60%,80%{transform:scale(1.1) rotate(-3deg);}100%{transform:scale(1) rotate(0);} }
            @keyframes shakeX { 0%,100%{transform:translateX(0);}10%,30%,50%,70%,90%{transform:translateX(-10px);}20%,40%,60%,80%{transform:translateX(10px);} }
            @keyframes pulse { 0%{transform:scale(1);}50%{transform:scale(1.08);}100%{transform:scale(1);} }
          </style>
        </head>
        <body>
        """ + content + "</body></html>";
    }

    // Parse form params
    private static String getParam(String body, String key) {
        for (String param : body.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static double parseAmount(String body) {
        try {
            String amtStr = getParam(body, "amount");
            return Double.parseDouble(amtStr);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
import "./App.css";
import { Routes, Route, Navigate } from "react-router-dom";
import { CartProvider } from "./contexts/CartContext";

import HomePage from "./components/Home/HomePage.jsx";
import SignIn from "./components/Auth/SignIn.jsx";
import SignUp from "./components/Auth/SignUp.jsx";
import ForgotPassword from "./components/Auth/ForgotPassword.jsx";
import UserProfile from "./components/Auth/UserProfile";
import EditUserProfile from "./components/Auth/EditUserProfile";
import ReviewsPage from "./pages/ReviewsPage.jsx";
import BookDetail from "./components/Book/BookDetail.jsx";
import Cart from "./components/Cart/Cart.jsx";
import CheckoutPage from "./pages/CheckoutPage.jsx";
import OrderHistory from "./components/Auth/OrderHistory.jsx";
import RefundHistory from "./components/Auth/RefundHistory.jsx"
import AdministrationBoard from "./pages/admin/AdministrationBoard.jsx";
import AccountManagement from "./pages/admin/AccountManagement.jsx";
import BookManagement from "./pages/admin/BookManagement.jsx";
import CommentManagement from "./pages/admin/CommentManagement.jsx";
import OrderManagement from "./pages/admin/OrderManagement.jsx";
import AdminRoute from "./pages/admin/AdminRoute.jsx";
import ReservationManagement from "./pages/admin/ReservationManagement.jsx";
import FeedbackManagement from "./pages/admin/FeedbackManagement.jsx";
import ReservationPage from "./components/Reservation/ReservationPage.jsx";
import MyEbook from "./components/Auth/MyEbook.jsx"
import FeedbackPage from "./pages/FeedbackPage.jsx";

function App() {
  return (
      <CartProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/homepage" />} />
          <Route path="/homepage" element={<HomePage />} />
          <Route path="/book/:id" element={<BookDetail />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/signin" element={<SignIn />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/forgot" element={<ForgotPassword />} />
          <Route path="/userprofile" element={<UserProfile />} />
          <Route path="/edituserprofile" element={<EditUserProfile />} />
          <Route path="/reviews" element={<ReviewsPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/orderhistory" element={<OrderHistory />} />
          <Route path="/refundhistory" element={<RefundHistory />} />
          <Route path="/reservations" element={<ReservationPage />} />
          <Route path="/reserve/:bookId" element={<ReservationPage />} />
          <Route path="/feedback" element={<FeedbackPage />} />
          <Route path="/myebook" element={<MyEbook />} />
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdministrationBoard />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/accounts"
            element={
              <AdminRoute>
                <AccountManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/books"
            element={
              <AdminRoute>
                <BookManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/comments"
            element={
              <AdminRoute>
                <CommentManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/orders"
            element={
              <AdminRoute>
                <OrderManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/reservations"
            element={
              <AdminRoute>
                <ReservationManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/admin/feedback"
            element={
              <AdminRoute>
                <FeedbackManagement />
              </AdminRoute>
            }
          />
        </Routes>
      </CartProvider>
  );
}

export default App;

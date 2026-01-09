import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

vi.mock("../../../services/admin", () => ({
  fetchDashboardSummary: vi.fn(),
  fetchAdminNotifications: vi.fn(),
  createAdminNotification: vi.fn(),
  deleteAdminNotification: vi.fn(),
}));

import * as adminService from "../../../services/admin";
import AdministrationBoard from "../AdministrationBoard";

describe("AdministrationBoard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    adminService.fetchDashboardSummary.mockResolvedValue({
      totalRevenue: 3456.78,
      totalOrders: 42,
      pendingOrders: 5,
      totalUsers: 18,
      monthlySales: [
        { month: "2024-08", amount: 900 },
        { month: "2024-09", amount: 1100 },
      ],
    });
    adminService.fetchAdminNotifications.mockResolvedValue([
      {
        notificationId: 1,
        title: "Maintenance",
        message: "Platform upgrades tonight",
        type: "maintenance",
        pinned: true,
      },
    ]);
    adminService.createAdminNotification.mockResolvedValue({
      notificationId: 2,
      title: "Sale",
      message: "Buy 2 get 1",
      type: "promotion",
      pinned: false,
    });
    adminService.deleteAdminNotification.mockResolvedValue();
  });

  it("renders metrics and navigation cards", async () => {
    render(
      <MemoryRouter>
        <AdministrationBoard />
      </MemoryRouter>
    );

    await waitFor(() => expect(adminService.fetchDashboardSummary).toHaveBeenCalled());

    expect(screen.getByText("Administration Board")).toBeInTheDocument();
    expect(screen.getByText(/Total Revenue/i)).toBeInTheDocument();
    expect(screen.getByText(/Orders in System/i)).toBeInTheDocument();
    expect(screen.getByText("Account Management")).toBeInTheDocument();
    expect(screen.getByText("Comment Moderation")).toBeInTheDocument();
    expect(screen.getByText("Reservation Management")).toBeInTheDocument();
  });

  it("allows publishing and removing notifications", async () => {
    render(
      <MemoryRouter>
        <AdministrationBoard />
      </MemoryRouter>
    );

    await waitFor(() => expect(adminService.fetchAdminNotifications).toHaveBeenCalled());

    const titleInput = screen.getByPlaceholderText("Title");
    await userEvent.type(titleInput, "Flash Sale");
    const messageInput = screen.getByPlaceholderText("What should people know?");
    await userEvent.type(messageInput, "Up to 40% off");
    await userEvent.click(screen.getByRole("button", { name: /Publish/i }));

    await waitFor(() => expect(adminService.createAdminNotification).toHaveBeenCalled());
    expect(screen.getByText("Sale")).toBeInTheDocument();

    const removeButtons = await screen.findAllByRole("button", { name: /Remove/i });
    await userEvent.click(removeButtons[0]);
    await waitFor(() => expect(adminService.deleteAdminNotification).toHaveBeenCalled());
  });
});

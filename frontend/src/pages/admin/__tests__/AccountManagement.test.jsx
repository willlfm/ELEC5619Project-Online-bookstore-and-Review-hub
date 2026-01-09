import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

vi.mock("../../../services/admin", () => ({
  fetchAdminUsers: vi.fn(),
  createAdminUser: vi.fn(),
  updateAdminUser: vi.fn(),
  deleteAdminUser: vi.fn(),
}));

import * as adminService from "../../../services/admin";
import AccountManagement from "../AccountManagement";

describe("AccountManagement", () => {
  beforeAll(() => {
    window.scrollTo = vi.fn();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    adminService.fetchAdminUsers.mockResolvedValue({
      content: [
        {
          userId: 0,
          username: "test_admin1",
          email: "admin@example.com",
          name: "Administrator",
          phone: "000000",
          role: "admin",
        },
        {
          userId: 1,
          username: "librarian",
          email: "librarian@example.com",
          name: "Libby",
          phone: "123456",
          role: "admin",
        },
      ],
      number: 0,
      size: 10,
      totalPages: 1,
    });
    adminService.createAdminUser.mockResolvedValue({
      userId: 2,
      username: "newmanager",
      email: "manager@example.com",
      name: "Manager",
      phone: "987654",
      role: "admin",
    });
  });

  it("loads users and allows creating a new account", async () => {
    render(<AccountManagement />);

    await waitFor(() => expect(adminService.fetchAdminUsers).toHaveBeenCalled());
    expect(screen.getByText("librarian")).toBeInTheDocument();
    expect(screen.queryByText("test_admin1")).not.toBeInTheDocument();

    await userEvent.type(screen.getByLabelText(/Username/i), "newmanager");
    await userEvent.type(screen.getByLabelText(/Email/i), "manager@example.com");
    await userEvent.type(screen.getByLabelText(/Display name/i), "Manager");
    await userEvent.type(screen.getByLabelText(/Phone/i), "987654");
    await userEvent.selectOptions(screen.getByLabelText(/Role/i), "admin");
    await userEvent.type(screen.getByLabelText(/Password/i), "safePass123");

    await userEvent.click(screen.getByRole("button", { name: /Create account/i }));

    await waitFor(() => expect(adminService.createAdminUser).toHaveBeenCalledWith({
      username: "newmanager",
      email: "manager@example.com",
      name: "Manager",
      phone: "987654",
      role: "admin",
      password: "safePass123",
    }));

    await waitFor(() => expect(screen.getByText("newmanager")).toBeInTheDocument());
  });
});

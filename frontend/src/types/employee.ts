export interface EmployeeResponse {
  id: number;
  name: string;
  email: string;
  departmentId: number;
  departmentName: string;
  role: string;
  position: string | null;
  active: boolean;
  paidLeaveBalance: number;
}

export interface LoginRequest {
  employeeId: number;
  password: string;
}

export interface LoginResponse {
  employee: EmployeeResponse;
}

export interface EmployeeCreateRequest {
  name: string;
  email: string;
  departmentId: number;
  role: string;
  position?: string;
  password: string;
}

export interface EmployeeUpdateRequest {
  name: string;
  email: string;
  departmentId: number;
  role: string;
  position?: string;
}

export interface PasswordResetRequest {
  newPassword: string;
}

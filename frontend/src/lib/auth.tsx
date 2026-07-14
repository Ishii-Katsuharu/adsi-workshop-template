"use client";

import { createContext, useContext, useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type { EmployeeResponse, LoginRequest } from "@/types/employee";
import { apiFetch } from "@/lib/api-client";

interface AuthContextValue {
  user: EmployeeResponse | null;
  loading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<EmployeeResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch<EmployeeResponse>("/auth/me")
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await apiFetch<{ employee: EmployeeResponse }>("/auth/login", {
      method: "POST",
      body: JSON.stringify(request),
    });
    setUser(response.employee);
  }, []);

  const logout = useCallback(async () => {
    await apiFetch<void>("/auth/logout", { method: "POST" });
    setUser(null);
  }, []);

  return (
    <AuthContext value={{ user, loading, login, logout }}>
      {children}
    </AuthContext>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

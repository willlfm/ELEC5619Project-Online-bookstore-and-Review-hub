import axios from "axios";

export const API_BASE = "/"; // Spring Boot 后端根 URL

export const apiClient = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    }
  });

// 导出 apiRequest 函数
export const apiRequest = async (url, options = {}) => {
    try {
        const response = await apiClient({
            url,
            method: options.method || 'GET',
            data: options.body,
            ...options
        });
        return response;
    } catch (error) {
        throw error;
    }
};


// ICanBusService.aidl
package com.lotusreichhart.canbussimulator;

// Giao diện AIDL để điều khiển dịch vụ mô phỏng CAN Bus
interface ICanBusService {
    // Bắt đầu quá trình mô phỏng phát dữ liệu CAN
    void startSimulation();

    // Dừng quá trình mô phỏng phát dữ liệu CAN
    void stopSimulation();

    // Chèn một CAN frame giả lập cụ thể dựa trên CAN ID truyền vào
    void injectDummyFrame(int canId);
}
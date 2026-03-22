import Shared

extension Data {

    /// 将 Swift `Data` 转为 KMP `KotlinByteArray`（如上传照片接口）。
    func toKotlinByteArray() -> KotlinByteArray {
        let array = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            array.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return array
    }
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.likbilen.protorpc;

public final class MessageProto {
  private MessageProto() {}
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static final com.google.protobuf.Descriptors.FileDescriptor
      descriptor = buildDescriptor();
  private static
      com.google.protobuf.Descriptors.FileDescriptor
      buildDescriptor() {
    java.lang.String descriptorData =
      "\n\rMessage.proto\022\010protorpc\"Q\n\007Message\022\034\n\004" +
      "type\030\001 \001(\0162\016.protorpc.Type\022\n\n\002id\030\002 \001(\r\022\014" +
      "\n\004name\030\003 \001(\t\022\016\n\006buffer\030\004 \001(\014\"c\n\022Descript" +
      "orResponse\022\014\n\004desc\030\001 \002(\014\022*\n\004deps\030\002 \003(\0132\034" +
      ".protorpc.DescriptorResponse\022\023\n\013serviceN" +
      "ame\030\003 \001(\t*\225\001\n\004Type\022\013\n\007REQEUST\020\001\022\014\n\010RESPO" +
      "NSE\020\002\022\023\n\017RESPONSE_CANCEL\020\003\022\034\n\030RESPONSE_N" +
      "OT_IMPLEMENTED\020\004\022\016\n\nDISCONNECT\020\005\022\026\n\022DESC" +
      "RIPTOR_REQUEST\020\006\022\027\n\023DESCRIPTOR_RESPONSE\020" +
      "\007B%\n\025com.likbilen.protorpcB\014MessageProto";
    try {
      return com.google.protobuf.Descriptors.FileDescriptor
        .internalBuildGeneratedFileFrom(descriptorData,
          new com.google.protobuf.Descriptors.FileDescriptor[] {
          });
    } catch (Exception e) {
      throw new RuntimeException(
        "Failed to parse protocol buffer descriptor for " +
        "\"Message.proto\".", e);
    }
  }
  
  public static enum Type {
    REQEUST(0, 1),
    RESPONSE(1, 2),
    RESPONSE_CANCEL(2, 3),
    RESPONSE_NOT_IMPLEMENTED(3, 4),
    DISCONNECT(4, 5),
    DESCRIPTOR_REQUEST(5, 6),
    DESCRIPTOR_RESPONSE(6, 7),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static Type valueOf(int value) {
      switch (value) {
        case 1: return REQEUST;
        case 2: return RESPONSE;
        case 3: return RESPONSE_CANCEL;
        case 4: return RESPONSE_NOT_IMPLEMENTED;
        case 5: return DISCONNECT;
        case 6: return DESCRIPTOR_REQUEST;
        case 7: return DESCRIPTOR_RESPONSE;
        default: return null;
      }
    }
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.likbilen.protorpc.MessageProto.getDescriptor().getEnumTypes().get(0);
    }
    
    private static final Type[] VALUES = {
      REQEUST, RESPONSE, RESPONSE_CANCEL, RESPONSE_NOT_IMPLEMENTED, DISCONNECT, DESCRIPTOR_REQUEST, DESCRIPTOR_RESPONSE, 
    };
    public static Type valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private Type(int index, int value) {
      this.index = index;
      this.value = value;
    }
  }
  
  public static final class Message extends
      com.google.protobuf.GeneratedMessage {
    // Use Message.newBuilder() to construct.
    private Message() {}
    
    private static final Message defaultInstance = new Message();
    public static Message getDefaultInstance() {
      return defaultInstance;
    }
    
    public Message getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.likbilen.protorpc.MessageProto.internal_static_protorpc_Message_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.likbilen.protorpc.MessageProto.internal_static_protorpc_Message_fieldAccessorTable;
    }
    
    // optional .protorpc.Type type = 1;
    private boolean hasType;
    private com.likbilen.protorpc.MessageProto.Type type_ = com.likbilen.protorpc.MessageProto.Type.REQEUST;
    public boolean hasType() { return hasType; }
    public com.likbilen.protorpc.MessageProto.Type getType() { return type_; }
    
    // optional uint32 id = 2;
    private boolean hasId;
    private int id_ = 0;
    public boolean hasId() { return hasId; }
    public int getId() { return id_; }
    
    // optional string name = 3;
    private boolean hasName;
    private java.lang.String name_ = "";
    public boolean hasName() { return hasName; }
    public java.lang.String getName() { return name_; }
    
    // optional bytes buffer = 4;
    private boolean hasBuffer;
    private com.google.protobuf.ByteString buffer_ = com.google.protobuf.ByteString.EMPTY;
    public boolean hasBuffer() { return hasBuffer; }
    public com.google.protobuf.ByteString getBuffer() { return buffer_; }
    
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.Message parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return new Builder(); }
    public Builder newBuilderForType() { return new Builder(); }
    public static Builder newBuilder(com.likbilen.protorpc.MessageProto.Message prototype) {
      return new Builder().mergeFrom(prototype);
    }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      // Construct using com.likbilen.protorpc.MessageProto.Message.newBuilder()
      private Builder() {}
      
      com.likbilen.protorpc.MessageProto.Message result = new com.likbilen.protorpc.MessageProto.Message();
      
      protected com.likbilen.protorpc.MessageProto.Message internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        result = new com.likbilen.protorpc.MessageProto.Message();
        return this;
      }
      
      public Builder clone() {
        return new Builder().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.likbilen.protorpc.MessageProto.Message.getDescriptor();
      }
      
      public com.likbilen.protorpc.MessageProto.Message getDefaultInstanceForType() {
        return com.likbilen.protorpc.MessageProto.Message.getDefaultInstance();
      }
      
      public com.likbilen.protorpc.MessageProto.Message build() {
        if (!isInitialized()) {
          throw new com.google.protobuf.UninitializedMessageException(
            result);
        }
        return buildPartial();
      }
      
      private com.likbilen.protorpc.MessageProto.Message buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw new com.google.protobuf.UninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.likbilen.protorpc.MessageProto.Message buildPartial() {
        com.likbilen.protorpc.MessageProto.Message returnMe = result;
        result = null;
        return returnMe;
      }
      
      
      // optional .protorpc.Type type = 1;
      public boolean hasType() {
        return result.hasType();
      }
      public com.likbilen.protorpc.MessageProto.Type getType() {
        return result.getType();
      }
      public Builder setType(com.likbilen.protorpc.MessageProto.Type value) {
        result.hasType = true;
        result.type_ = value;
        return this;
      }
      public Builder clearType() {
        result.hasType = false;
        result.type_ = com.likbilen.protorpc.MessageProto.Type.REQEUST;
        return this;
      }
      
      // optional uint32 id = 2;
      public boolean hasId() {
        return result.hasId();
      }
      public int getId() {
        return result.getId();
      }
      public Builder setId(int value) {
        result.hasId = true;
        result.id_ = value;
        return this;
      }
      public Builder clearId() {
        result.hasId = false;
        result.id_ = 0;
        return this;
      }
      
      // optional string name = 3;
      public boolean hasName() {
        return result.hasName();
      }
      public java.lang.String getName() {
        return result.getName();
      }
      public Builder setName(java.lang.String value) {
        result.hasName = true;
        result.name_ = value;
        return this;
      }
      public Builder clearName() {
        result.hasName = false;
        result.name_ = "";
        return this;
      }
      
      // optional bytes buffer = 4;
      public boolean hasBuffer() {
        return result.hasBuffer();
      }
      public com.google.protobuf.ByteString getBuffer() {
        return result.getBuffer();
      }
      public Builder setBuffer(com.google.protobuf.ByteString value) {
        result.hasBuffer = true;
        result.buffer_ = value;
        return this;
      }
      public Builder clearBuffer() {
        result.hasBuffer = false;
        result.buffer_ = com.google.protobuf.ByteString.EMPTY;
        return this;
      }
    }
  }
  
  public static final class DescriptorResponse extends
      com.google.protobuf.GeneratedMessage {
    // Use DescriptorResponse.newBuilder() to construct.
    private DescriptorResponse() {}
    
    private static final DescriptorResponse defaultInstance = new DescriptorResponse();
    public static DescriptorResponse getDefaultInstance() {
      return defaultInstance;
    }
    
    public DescriptorResponse getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.likbilen.protorpc.MessageProto.internal_static_protorpc_DescriptorResponse_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.likbilen.protorpc.MessageProto.internal_static_protorpc_DescriptorResponse_fieldAccessorTable;
    }
    
    // required bytes desc = 1;
    private boolean hasDesc;
    private com.google.protobuf.ByteString desc_ = com.google.protobuf.ByteString.EMPTY;
    public boolean hasDesc() { return hasDesc; }
    public com.google.protobuf.ByteString getDesc() { return desc_; }
    
    // repeated .protorpc.DescriptorResponse deps = 2;
    private java.util.List<com.likbilen.protorpc.MessageProto.DescriptorResponse> deps_ =
      java.util.Collections.emptyList();
    public java.util.List<com.likbilen.protorpc.MessageProto.DescriptorResponse> getDepsList() {
      return deps_;
    }
    public int getDepsCount() { return deps_.size(); }
    public com.likbilen.protorpc.MessageProto.DescriptorResponse getDeps(int index) {
      return deps_.get(index);
    }
    
    // optional string serviceName = 3;
    private boolean hasServiceName;
    private java.lang.String serviceName_ = "";
    public boolean hasServiceName() { return hasServiceName; }
    public java.lang.String getServiceName() { return serviceName_; }
    
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.likbilen.protorpc.MessageProto.DescriptorResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistry extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return new Builder(); }
    public Builder newBuilderForType() { return new Builder(); }
    public static Builder newBuilder(com.likbilen.protorpc.MessageProto.DescriptorResponse prototype) {
      return new Builder().mergeFrom(prototype);
    }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      // Construct using com.likbilen.protorpc.MessageProto.DescriptorResponse.newBuilder()
      private Builder() {}
      
      com.likbilen.protorpc.MessageProto.DescriptorResponse result = new com.likbilen.protorpc.MessageProto.DescriptorResponse();
      
      protected com.likbilen.protorpc.MessageProto.DescriptorResponse internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        result = new com.likbilen.protorpc.MessageProto.DescriptorResponse();
        return this;
      }
      
      public Builder clone() {
        return new Builder().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.likbilen.protorpc.MessageProto.DescriptorResponse.getDescriptor();
      }
      
      public com.likbilen.protorpc.MessageProto.DescriptorResponse getDefaultInstanceForType() {
        return com.likbilen.protorpc.MessageProto.DescriptorResponse.getDefaultInstance();
      }
      
      public com.likbilen.protorpc.MessageProto.DescriptorResponse build() {
        if (!isInitialized()) {
          throw new com.google.protobuf.UninitializedMessageException(
            result);
        }
        return buildPartial();
      }
      
      private com.likbilen.protorpc.MessageProto.DescriptorResponse buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw new com.google.protobuf.UninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.likbilen.protorpc.MessageProto.DescriptorResponse buildPartial() {
        if (result.deps_ != java.util.Collections.EMPTY_LIST) {
          result.deps_ =
            java.util.Collections.unmodifiableList(result.deps_);
        }
        com.likbilen.protorpc.MessageProto.DescriptorResponse returnMe = result;
        result = null;
        return returnMe;
      }
      
      
      // required bytes desc = 1;
      public boolean hasDesc() {
        return result.hasDesc();
      }
      public com.google.protobuf.ByteString getDesc() {
        return result.getDesc();
      }
      public Builder setDesc(com.google.protobuf.ByteString value) {
        result.hasDesc = true;
        result.desc_ = value;
        return this;
      }
      public Builder clearDesc() {
        result.hasDesc = false;
        result.desc_ = com.google.protobuf.ByteString.EMPTY;
        return this;
      }
      
      // repeated .protorpc.DescriptorResponse deps = 2;
      public java.util.List<com.likbilen.protorpc.MessageProto.DescriptorResponse> getDepsList() {
        return java.util.Collections.unmodifiableList(result.deps_);
      }
      public int getDepsCount() {
        return result.getDepsCount();
      }
      public com.likbilen.protorpc.MessageProto.DescriptorResponse getDeps(int index) {
        return result.getDeps(index);
      }
      public Builder setDeps(int index, com.likbilen.protorpc.MessageProto.DescriptorResponse value) {
        result.deps_.set(index, value);
        return this;
      }
      public Builder setDeps(int index, com.likbilen.protorpc.MessageProto.DescriptorResponse.Builder builderForValue) {
        result.deps_.set(index, builderForValue.build());
        return this;
      }
      public Builder addDeps(com.likbilen.protorpc.MessageProto.DescriptorResponse value) {
        if (result.deps_.isEmpty()) {
          result.deps_ = new java.util.ArrayList<com.likbilen.protorpc.MessageProto.DescriptorResponse>();
        }
        result.deps_.add(value);
        return this;
      }
      public Builder addDeps(com.likbilen.protorpc.MessageProto.DescriptorResponse.Builder builderForValue) {
        if (result.deps_.isEmpty()) {
          result.deps_ = new java.util.ArrayList<com.likbilen.protorpc.MessageProto.DescriptorResponse>();
        }
        result.deps_.add(builderForValue.build());
        return this;
      }
      public Builder addAllDeps(
          java.lang.Iterable<? extends com.likbilen.protorpc.MessageProto.DescriptorResponse> values) {
        if (result.deps_.isEmpty()) {
          result.deps_ = new java.util.ArrayList<com.likbilen.protorpc.MessageProto.DescriptorResponse>();
        }
        super.addAll(values, result.deps_);
        return this;
      }
      public Builder clearDeps() {
        result.deps_ = java.util.Collections.emptyList();
        return this;
      }
      
      // optional string serviceName = 3;
      public boolean hasServiceName() {
        return result.hasServiceName();
      }
      public java.lang.String getServiceName() {
        return result.getServiceName();
      }
      public Builder setServiceName(java.lang.String value) {
        result.hasServiceName = true;
        result.serviceName_ = value;
        return this;
      }
      public Builder clearServiceName() {
        result.hasServiceName = false;
        result.serviceName_ = "";
        return this;
      }
    }
  }
  
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_protorpc_Message_descriptor =
      getDescriptor().getMessageTypes().get(0);
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_protorpc_Message_fieldAccessorTable = new
        com.google.protobuf.GeneratedMessage.FieldAccessorTable(
          internal_static_protorpc_Message_descriptor,
          new java.lang.String[] { "Type", "Id", "Name", "Buffer", },
          com.likbilen.protorpc.MessageProto.Message.class,
          com.likbilen.protorpc.MessageProto.Message.Builder.class);
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_protorpc_DescriptorResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_protorpc_DescriptorResponse_fieldAccessorTable = new
        com.google.protobuf.GeneratedMessage.FieldAccessorTable(
          internal_static_protorpc_DescriptorResponse_descriptor,
          new java.lang.String[] { "Desc", "Deps", "ServiceName", },
          com.likbilen.protorpc.MessageProto.DescriptorResponse.class,
          com.likbilen.protorpc.MessageProto.DescriptorResponse.Builder.class);
}

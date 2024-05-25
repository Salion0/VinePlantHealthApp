import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import os
import datetime
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, f1_score , confusion_matrix
from tensorflow.keras import models
from keras.layers import Dense, Dropout, Flatten
import itertools
import os
#dataset https://www.kaggle.com/datasets/rm1000/augmented-grape-disease-detection-dataset

dataPath=r"C:\Users\edoar\OneDrive\Desktop\final Mobile model\Final Training Data"
# Define the base directory where your class folders are located
base_dir = dataPath

# Define the destination base directories
train_dir = os.path.join(dataPath, "Train")
test_dir = os.path.join(dataPath, "Test")
valid_dir = os.path.join(dataPath, "Valid")

BATCH_SIZE = 32
IMAGE_HEIGHT = 256
IMAGE_WIDTH = 256
train_dataset = keras.utils.image_dataset_from_directory(
    os.path.join(dataPath, 'Train'),
    image_size = (IMAGE_HEIGHT, IMAGE_WIDTH),
    batch_size = BATCH_SIZE,
    label_mode = 'categorical'
)

valid_dataset = keras.utils.image_dataset_from_directory(
    os.path.join(dataPath, 'Valid'),
    image_size = (IMAGE_HEIGHT, IMAGE_WIDTH),
    batch_size = BATCH_SIZE,
    label_mode = 'categorical'
)

test_dataset = keras.utils.image_dataset_from_directory(
    os.path.join(dataPath, 'Test'),
    image_size = (IMAGE_HEIGHT, IMAGE_WIDTH),
    batch_size = BATCH_SIZE,
    label_mode = 'categorical',
    shuffle=False
)


data_augmentation = keras.Sequential([
  layers.RandomFlip("horizontal"), # Applies horizontal flipping to a random 50% of the images
  layers.RandomRotation(0.1), # Rotates the input images by a random value in the range[–10%, +10%] (fraction of full circle [-36°, 36°])
  layers.RandomZoom(0.1), # Zooms in or out of the image by a random factor in the range [-20%, +20%]
  layers.RandomContrast(0.1),
],
name = "AugmentationLayer"
)

model = keras.Sequential(name="FromScratch")
model.add(data_augmentation)
model.add(layers.Rescaling(1./255))

model.add(layers.Conv2D(32, kernel_size = 3, activation = "relu6", padding = "same", input_shape = (256, 256,3)))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.BatchNormalization())

model.add(layers.Conv2D(64, kernel_size = 3, activation='relu', padding = "same"))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.BatchNormalization())

model.add(layers.Conv2D(128, kernel_size = 3, activation='relu', padding = "same"))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.BatchNormalization())

model.add(layers.Conv2D(256, kernel_size = 3, activation='relu', padding = "same"))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.BatchNormalization())

model.add(layers.Conv2D(512, kernel_size = 3, activation='relu', padding = "same"))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.BatchNormalization())

model.add(layers.Conv2D(512, kernel_size = 3, activation='relu', padding = "same"))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.BatchNormalization())

model.add(layers.Flatten())
model.add(layers.Dropout(0.5))

model.add(layers.Dense(256,activation="relu"))
model.add(layers.Dense(4,activation="softmax"))

model.build(input_shape=(None, 256, 256, 3))
model.add(Flatten())
model.compile(loss="categorical_crossentropy",
              optimizer="adam",
              metrics=["accuracy"])

model.summary()

modelName = "prova2"
scratchPath=scratchPath = os.path.join(dataPath, 'models', 'from_scratch')
modelPath = os.path.join(scratchPath, modelName + ".keras")

save_best_model = tf.keras.callbacks.ModelCheckpoint(modelPath, verbose=True, monitor='val_loss', save_best_only=True)

earlyStopping = tf.keras.callbacks.EarlyStopping(monitor='val_loss', patience=5)

history = model.fit(
    train_dataset,
    epochs=20,
    validation_data=valid_dataset,
    validation_steps=int(np.ceil(len(valid_dataset)/BATCH_SIZE-1)),
    callbacks=[earlyStopping, save_best_model]
  )

cnn_base = tf.keras.applications.VGG16(
    include_top=False,
    weights="imagenet",
    input_shape=(IMAGE_HEIGHT, IMAGE_WIDTH, 3),
    pooling='max',
)
cnn_base.summary()

resize_and_rescale = tf.keras.Sequential([
  layers.Resizing(IMAGE_HEIGHT, IMAGE_WIDTH),
  layers.Rescaling(1./255)
])

cnn_base.trainable = False

print('This is the number of trainable weights '
      'after freezing the conv base:', sum(np.prod(x.shape) for x in cnn_base.trainable_weights))

data_augmentation = keras.Sequential([
  layers.RandomFlip("horizontal"), # Applies horizontal flipping to a random 50% of the images
  layers.RandomRotation(0.1), # Rotates the input images by a random value in the range[–10%, +10%] (fraction of full circle [-36°, 36°])
  layers.RandomZoom(0.1), # Zooms in or out of the image by a random factor in the range [-20%, +20%]
  layers.RandomContrast(0.1),
],
name = "AugmentationLayer"
)
pretrained_model = tf.keras.Sequential([
    resize_and_rescale,
    data_augmentation,
    cnn_base,
    layers.Flatten(),
    layers.Dense(256, activation="relu"),
    layers.BatchNormalization(),
    layers.Dropout(0.4),
    layers.Dense(4, activation = 'softmax')
])


pretrained_model.compile(
    loss = 'categorical_crossentropy',
    optimizer = 'adam',
    metrics = ["accuracy"]
)

pretrained_model.build(input_shape=(None, IMAGE_HEIGHT, IMAGE_WIDTH, 3))

pretrained_model.summary()

modelName = "FE_base_256d"
fExtractorPath = os.path.join(dataPath, 'models', 'feature_extraction')
modelPath = os.path.join(fExtractorPath, modelName + ".keras")

save_best_model = tf.keras.callbacks.ModelCheckpoint(modelPath, monitor='val_loss', verbose = 1, save_best_only=True)

earlyStopping = tf.keras.callbacks.EarlyStopping(monitor='val_loss', patience=5)

history = pretrained_model.fit(
    train_dataset,
    epochs=20,
    validation_data=valid_dataset,
    validation_steps=int(np.ceil(len(valid_dataset)/BATCH_SIZE-1)),
    callbacks=[earlyStopping, save_best_model]
  )